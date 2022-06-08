/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.ifd.IFD;
import edu.rit.se.design.arcode.fspecminer.ifd.MethodRepresentation;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class GRAAMBuilder {
    static final String SERIALIZED_GRAAM_EXTENSION = ".srz";
    static final String DOT_GRAPH_EXTENSION = ".dot";
    static Set<String> COLLECTION_CLASS_NAMES = new HashSet<>();
    static{
        COLLECTION_CLASS_NAMES.add( "Ljava/util/Set" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/List" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/HashSet" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/ArrayList" );
    }

    static Random ST_RANDOM = new Random();

    public static GRAAM buildGRAAM(PrimaryAPIUsageGraph primaryAPIUsageGraph, IFD ifd) {
        GRAAM graam = createFrom(primaryAPIUsageGraph);
        mergeNestedInitNodes(graam);
        mergeNewAndInitNodes(graam);
        addIFDBasedEdges(graam, ifd);
        fixBoundaryNodes(graam);
        removeRedundantNodes(graam);
        return graam;
    }

    static StatementRepresentation cloneStatementRepresentation( StatementRepresentation statementRepresentation ){
        StatementRepresentation clonedStatementRepresentation = new StatementRepresentation(statementRepresentation.frameworkClass,
                statementRepresentation.frameworkMethod, statementRepresentation.isAbstractOrInterfaceConstructorNode,
                statementRepresentation.isStaticMethod, statementRepresentation.isPublicMethod, statementRepresentation.originClass,
                statementRepresentation.originMethod, statementRepresentation.originalLineNumber, statementRepresentation.apiType,
                statementRepresentation.originalStatementIIndex, statementRepresentation.originalStatementCGNodeId);
        clonedStatementRepresentation.originalStatementIIndex = ST_RANDOM.nextInt(1000000) ;
        clonedStatementRepresentation.originalStatementCGNodeId = ST_RANDOM.nextInt(1000000);

        return clonedStatementRepresentation;
    }

    public static DirectedGraphNode cloneNodeFromScratch( DirectedGraphNode directedGraphNode ){
        DirectedGraphNode clonedNode = directedGraphNode.clone();
        if( clonedNode instanceof FrameworkRelatedNode ){
            StatementRepresentation clonedStatementRepresentation = cloneStatementRepresentation(
                    ((FrameworkRelatedNode) clonedNode).statementRepresentation );
            ((FrameworkRelatedNode) clonedNode).statementRepresentation = clonedStatementRepresentation;
        }
        return clonedNode;
    }

    public static Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> cloneFromScratch(GRAAM graam ){
        GRAAM clonedGraam = new GRAAM(graam.primaryAPIUsageGraph );
        Map<DirectedGraphNode, DirectedGraphNode> nodeMapping = new HashMap<>();
        graam.iterator().forEachRemaining( graamNode -> {
            DirectedGraphNode clonedGraamNode = graamNode.clone(); // cloneNodeFromScratch( graamNode );
            clonedGraam.addNode( clonedGraamNode );
            nodeMapping.put( graamNode, clonedGraamNode );
        } );

        graam.iterator().forEachRemaining( graamNode -> {
            Arrays.stream(GRAAMEdgeType.values()).iterator().forEachRemaining(graamEdgeType -> {
                graam.getSuccNodes( graamNode, graamEdgeType ).forEach( graphNodeSucc -> clonedGraam.addEdge( nodeMapping.get(graamNode), nodeMapping.get(graphNodeSucc), graamEdgeType));
            });
        });
//        return buildGRAAM(graam.primaryAPIUsageGraph, ifd);
        return new ImmutablePair<>(clonedGraam, nodeMapping);
    }

    // Translates a primaryAPIUsageGraph to a GRAAM
    static GRAAM createFrom(PrimaryAPIUsageGraph primaryAPIUsageGraph) {
        GRAAM graam = new GRAAM(primaryAPIUsageGraph);
        primaryAPIUsageGraph.iterator().forEachRemaining(graphNode -> graam.addNode(graphNode));

        // Find all the data dependencies in the primaryAPIUsageGraph and for each one add an @GRAAMEdgeType.EXPLICIT_DATA_DEP edge in the
        // created GRAAM.
        primaryAPIUsageGraph.iterator().forEachRemaining(graphNode -> {
            if (primaryAPIUsageGraph.getPredNodeCount(graphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY) > 0)
                primaryAPIUsageGraph.getPredNodes(graphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY).forEach(graphNodePred -> {
                    if (!graam.hasEdge(graphNodePred, graphNode))
                        graam.addEdge(graphNodePred, graphNode, GRAAMEdgeType.EXPLICIT_DATA_DEP);
                });
        });
        return graam;
    }

    /*
        Finds nested constructors that are called for an object instantiation (e.g. New A -> Init A() -> Init A(String) -> ...)
        In a GRAAM, all these init nodes are connected to the same new node via EXPLICIT_DATA_DEP edges.
        We keep the init node which has another EXPLICIT_DATA_DEP edge rather than to its new node (another data dependency).
        If all the init nodes have no other edge to any other nodes rather than their new node, then we keep the simplest Init node
        (shorter in the length of title) and remove the rest.
     */
    static void mergeNestedInitNodes(GRAAM graam) {
        Map<FrameworkRelatedNode, Set<FrameworkRelatedNode>> newAndInitNodesMap = findNewAndInitNodesMap(graam);
        newAndInitNodesMap.forEach((newNode, initNodes) -> {
            if (initNodes.size() < 2)
                return;
            FrameworkRelatedNode toBeKeptNode = null;
            List<FrameworkRelatedNode> initNodesWithMoreThanOnePred = initNodes.stream().filter(frameworkRelatedNode -> graam.getPredNodeCount(frameworkRelatedNode) > 1).
                    collect(Collectors.toList());
            if (initNodesWithMoreThanOnePred.size() > 0)
                toBeKeptNode = initNodesWithMoreThanOnePred.get(0);

            if (toBeKeptNode == null)
                toBeKeptNode = initNodes.stream().sorted((o1, o2) -> Integer.compare(o1.getTitle().length(), o2.getTitle().length())).
                        collect(Collectors.toList()).get(0);

            FrameworkRelatedNode finalToBeKeptNode = toBeKeptNode;
            initNodes.stream().filter(frameworkRelatedNode -> !frameworkRelatedNode.equals(finalToBeKeptNode)).
                    collect(Collectors.toList()).forEach(toBeRemovedNode -> {
                graam.removeNodeAndEdges(toBeRemovedNode);
            });
        });
    }

    /*     1. Disconnects outgoing GRAAMEdgeType.EXPLICIT_DATA_DEP edges from newNode and connects them to initNode as outgoing edges.
         2. Disconnects incoming GRAAMEdgeType.EXPLICIT_DATA_DEP edges to newNode and connects them to its successors. In other word,
            it connects newNode's predecessors (GRAAMEdgeType.EXPLICIT_DATA_DEP edges) to it's successors (GRAAMEdgeType.EXPLICIT_DATA_DEP edges)).
         3. Removes the newNode.*/
    static void mergeNewAndInitNodes(GRAAM graam) {
        Map<FrameworkRelatedNode, Set<FrameworkRelatedNode>> newAndInitNodesMap = findNewAndInitNodesMap(graam);
        newAndInitNodesMap.forEach((newNode, initNodes) -> {
            initNodes.forEach(initNode -> {
                graam.getPredNodes(newNode).forEach(newPred -> {
                    if (!graam.hasEdge(newPred, initNode, GRAAMEdgeType.EXPLICIT_DATA_DEP))
                        graam.addEdge(newPred, initNode, GRAAMEdgeType.EXPLICIT_DATA_DEP);
                });
                graam.getSuccNodes(newNode).forEach(newSucc -> {
                    // Here we check to be sure that we are not adding a self-loop edge
                    if (!newSucc.equals(initNode) && !graam.hasEdge(initNode, newSucc, GRAAMEdgeType.EXPLICIT_DATA_DEP))
                        graam.addEdge(initNode, newSucc, GRAAMEdgeType.EXPLICIT_DATA_DEP);
                });
            });
            graam.removeNodeAndEdges(newNode);
        });
    }

    // Assumption: Currently, each NewNode is connected to its InitNode via an OrderConstraint edge in the GRAAM
    static Map<FrameworkRelatedNode, Set<FrameworkRelatedNode>> findNewAndInitNodesMap(GRAAM graam) {
        Map<FrameworkRelatedNode, Set<FrameworkRelatedNode>> newAndInitNodesMap = new HashMap<>();
        graam.iterator().forEachRemaining(graphNode -> {
            if (!isNewNode(graphNode))
                return;
            // Since the GRAAM is built based on only data dependency edges, among the successors of the new node we can find all its init nodes.
            graam.getSuccNodes(graphNode).forEach(newNodeSucc -> {
                if (!isInitNodeOf((FrameworkRelatedNode) newNodeSucc, (FrameworkRelatedNode) graphNode))
                    return;
                if (!newAndInitNodesMap.containsKey(graphNode))
                    newAndInitNodesMap.put((FrameworkRelatedNode) graphNode, new HashSet<>());
                newAndInitNodesMap.get((FrameworkRelatedNode) graphNode).add((FrameworkRelatedNode) newNodeSucc);
            });

        });
        return newAndInitNodesMap;
    }

    static boolean isInitNodeOf(FrameworkRelatedNode initNode, FrameworkRelatedNode newNode) {
        if (!newNode.isNewObjectNode() || !initNode.isInitNode())
            return false;
        return initNode.getFrameworkRelatedClass().equals(newNode.getFrameworkRelatedClass());
    }

    static boolean isNewNode(DirectedGraphNode graphNode) {
        return graphNode instanceof FrameworkRelatedNode && ((FrameworkRelatedNode) graphNode).isNewObjectNode();
    }

    // Adds GRAAMEdgeType.IMPLICIT_DATA_DEP edges based on IFD
    static void addIFDBasedEdges(GRAAM graam, IFD ifd) {
        Map<MethodRepresentation, Set<DirectedGraphNode>> ifdGraamNodeMapping = createIfdGraamNodeMapping(ifd, graam);
        ifdGraamNodeMapping.forEach((ifdNode, mappedGraamNodesToIfdNode) -> {
            ifd.getSuccNodes(ifdNode).forEach(ifdNodeSucc -> {
                if (ifdGraamNodeMapping.get(ifdNodeSucc) != null)
                    ifdGraamNodeMapping.get(ifdNodeSucc).forEach(toGraamNode -> {
                        mappedGraamNodesToIfdNode.forEach(fromGraamNode -> {
                            // Avoids self-loops
                            if (fromGraamNode.equals(toGraamNode))
                                return;
                            if (!graam.hasEdge(fromGraamNode, toGraamNode, GRAAMEdgeType.IMPLICIT_DATA_DEP))
                                graam.addEdge(fromGraamNode, toGraamNode, GRAAMEdgeType.IMPLICIT_DATA_DEP);
                        });
                    });
            });
        });
    }

    static Map<MethodRepresentation, Set<DirectedGraphNode>> createIfdGraamNodeMapping(IFD ifd, GRAAM graam) {
        Map<MethodRepresentation, Set<DirectedGraphNode>> ifdGraamNodeMapping = new HashMap<>();
        ifd.iterator().forEachRemaining(methodRepresentation -> {
            if (methodRepresentation.toString().contains("LoginContext"))
                System.out.print("");
            graam.iterator().forEachRemaining(graphNode -> {
                if (!(graphNode instanceof FrameworkRelatedNode))
                    return;
                if (!areTheSame(methodRepresentation, (FrameworkRelatedNode) graphNode))
                    return;
                if (!ifdGraamNodeMapping.containsKey(methodRepresentation))
                    ifdGraamNodeMapping.put(methodRepresentation, new HashSet<>());
                ifdGraamNodeMapping.get(methodRepresentation).add(graphNode);
            });
        });
        return ifdGraamNodeMapping;
    }

    static boolean areTheSame(MethodRepresentation methodRepresentation, FrameworkRelatedNode graphNode) {
        String methodRepresentationToString = methodRepresentation.getClassNode().name + "." + methodRepresentation.getMethodNode().name + methodRepresentation.getMethodNode().desc;
        String graphNodeToString = graphNode.getFrameworkRelatedClass() + "." + graphNode.getFrameworkRelatedMethod();
        return methodRepresentationToString.equals(graphNodeToString);
    }

    // Connects FSpecStartNode to nodes without parent and remove EndNodes
    static void fixBoundaryNodes(GRAAM graam) {
        StreamSupport.stream(graam.spliterator(), false).
                filter(graphNode -> graphNode instanceof NonFrameworkBoundaryNode).collect(Collectors.toList()).
                forEach(graphBoundaryNode -> graam.removeNodeAndEdges(graphBoundaryNode));

        NonFrameworkBoundaryNode startNode = new NonFrameworkBoundaryNode(NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE);
        graam.addNode(startNode);

        StreamSupport.stream(graam.spliterator(), false).
                filter(graphNode -> graam.getPredNodeCount(graphNode) == 0 && !(graphNode instanceof NonFrameworkBoundaryNode)).collect(Collectors.toList()).
                forEach(withoutParentNode -> graam.addEdge(startNode, withoutParentNode, GRAAMEdgeType.IMPLICIT_DATA_DEP));

        NonFrameworkBoundaryNode endNode = new NonFrameworkBoundaryNode(NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE);
        graam.addNode(endNode);

        StreamSupport.stream(graam.spliterator(), false).
                filter(graphNode -> graam.getSuccNodeCount(graphNode) == 0 && !(graphNode instanceof NonFrameworkBoundaryNode)).collect(Collectors.toList()).
                forEach(withoutChildNode -> graam.addEdge(withoutChildNode, endNode, GRAAMEdgeType.IMPLICIT_DATA_DEP));
    }

    // Similar nodes with the same parents (all the parents)
    static void removeRedundantNodes(GRAAM graam) {
        boolean redundantNodesFound = false;
        do {
            redundantNodesFound = false;
            List<DirectedGraphNode> graamNodes = StreamSupport.stream(graam.spliterator(),
                    false).collect(Collectors.toList());

            for (DirectedGraphNode firstGraamNode : graamNodes) {
                for (DirectedGraphNode secondGraamNode : graamNodes) {
                    if (firstGraamNode.equals(secondGraamNode))
                        continue;
                    if (areSemanticallyTheSame(firstGraamNode, secondGraamNode)) {
                        if (haveTheSameParents(graam, firstGraamNode, secondGraamNode)) {
                            graam.getSuccNodes(firstGraamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP).forEach(graamNode1Succ -> {
                                if (!graam.hasEdge(secondGraamNode, graamNode1Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP))
                                    graam.addEdge(secondGraamNode, graamNode1Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP);
                            });
                            graam.getSuccNodes(firstGraamNode, GRAAMEdgeType.IMPLICIT_DATA_DEP).forEach(graamNode1Succ -> {
                                if (!graam.hasEdge(secondGraamNode, graamNode1Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP))
                                    graam.addEdge(secondGraamNode, graamNode1Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP);
                            });
                            graam.removeNodeAndEdges(firstGraamNode);
                            redundantNodesFound = true;
                            break;
                        }
                    }
                }
                if (redundantNodesFound)
                    break;
            }

        } while (redundantNodesFound);
    }

    // Assumption: There are not two similar nodes in the GRAAMs with the same context
    public static Map<DirectedGraphNode, DirectedGraphNode> findContextBasedNodeMapping(GRAAM graam1, GRAAM graam2) {
        Map<DirectedGraphNode, DirectedGraphNode> mappedNodes = new HashMap<>();
        graam1.iterator().forEachRemaining(graam1Node -> {
            graam2.iterator().forEachRemaining(graam2Node -> {
                if (!mappedNodes.values().contains(graam2Node)) {
                    if (areContextBaseSemanticallyEqiuvalent(graam1, graam1Node, graam2, graam2Node)) {
                        mappedNodes.put(graam1Node, graam2Node);
                        return;
                    }
                }
            });
        });
        return mappedNodes;
    }

    public static boolean areContextBaseSemanticallyEqiuvalent(GRAAM graam1, DirectedGraphNode graam1Node, GRAAM graam2, DirectedGraphNode graam2Node ){
        if( !areSemanticallyTheSame(graam1Node, graam2Node)  )
            return false;

        List<DirectedGraphNode> graam2NodePreds = StreamSupport.stream( graam2.getPredNodes( graam2Node ).spliterator(), false).collect(Collectors.toList());
        List<DirectedGraphNode> graam1NodePreds = StreamSupport.stream( graam1.getPredNodes( graam1Node ).spliterator(), false).collect(Collectors.toList());

        if( graam2NodePreds.size() != graam1NodePreds.size() )
            return false;

        // They are semantically the same and both have no parents
        if( graam2NodePreds.size() == 0 )
            return true;

        // Collect all the possible node mappings for the preds that each graamNode1Pred is exactly mapped to one graamNode2Pred
        List<Map<DirectedGraphNode, DirectedGraphNode>> allPossibleNodeMappings =  StreamSupport.stream(
                /*findAllPossibleNodeMappings( graam1NodePreds, graam2NodePreds ).spliterator()*/
                findAllOneToOneNodeMappings( graam1NodePreds, graam2NodePreds ).spliterator()
                , false )
                .filter( nodeMapping -> nodeMapping.size() == graam2NodePreds.size() ).collect(Collectors.toList());

        // Collect those node mappings that the edge type between them and the given graam2Node/graam1Node are equivalent
        allPossibleNodeMappings = StreamSupport.stream( allPossibleNodeMappings.spliterator(), false ).filter( nodeMap -> {
            for (DirectedGraphNode graam1PredNode : nodeMap.keySet()) {
                DirectedGraphNode graam2PredNode = nodeMap.get( graam1PredNode );
                for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                    boolean graam1HasEdge = graam1.hasEdge( graam1PredNode, graam1Node, graamEdgeType );
                    boolean graam2HasEdge = graam2.hasEdge(graam2PredNode, graam2Node, graamEdgeType);

                    if ( (graam1HasEdge && !graam2HasEdge) || (graam2HasEdge && !graam1HasEdge) )
                        return false;
                }
            }
            return true;
        } ).collect(Collectors.toList());

        // Iterate over all the found valid mappings for the predecessors. If either of the found mappings
        // result in a context-base semantically equivalency, then return true
        for (Map<DirectedGraphNode, DirectedGraphNode> possibleNodeMapping : allPossibleNodeMappings) {
            boolean areSemanticallyTheSameForThisMapping = true;
            for (DirectedGraphNode graam1PredNode : possibleNodeMapping.keySet()) {
                DirectedGraphNode graam2PredNode = possibleNodeMapping.get( graam1PredNode );
                areSemanticallyTheSameForThisMapping = areSemanticallyTheSameForThisMapping &&
                        areContextBaseSemanticallyEqiuvalent( graam1, graam1PredNode, graam2, graam2PredNode );
            }
            if( areSemanticallyTheSameForThisMapping )
                return true;
        }

        return false;
    }

    static List<Map<DirectedGraphNode, DirectedGraphNode>> findAllOneToOneNodeMappings(List<DirectedGraphNode> graam1Nodes, List<DirectedGraphNode> graam2Nodes) {
        List<Map<DirectedGraphNode, DirectedGraphNode>> possibleMappings = new ArrayList<>();
        if( graam1Nodes.size() != graam2Nodes.size() )
            return possibleMappings;

        Map<DirectedGraphNode, Set<DirectedGraphNode>> nodeMapping = new HashMap<>();
        graam1Nodes.forEach( graam1Node -> {
            graam2Nodes.forEach( graam2Node -> {
                if( areSemanticallyTheSame( graam1Node, graam2Node ) ){
                    if( !nodeMapping.containsKey( graam1Node ) )
                        nodeMapping.put( graam1Node, new HashSet<>() );
                    nodeMapping.get( graam1Node ).add( graam2Node );
                }
            } );
        } );

        //TODO: check to see if values().containsAll() works as expected (e.g. checks whether all the graam2Nodes are in the value part of the map)
        if( !nodeMapping.keySet().containsAll( graam1Nodes ) || !nodeMapping.values().containsAll( graam2Nodes ) )
            return possibleMappings;

        createMappingCombinaition( possibleMappings, nodeMapping );

        return  possibleMappings;
    }

    static void createMappingCombinaition( List<Map<DirectedGraphNode, DirectedGraphNode>> soFarCreatedMapping,
                                           Map<DirectedGraphNode, Set<DirectedGraphNode>> toBeUsedMap ){
        Map<DirectedGraphNode, Set<DirectedGraphNode>> toBeUsedMapClone = new HashMap<>( toBeUsedMap );
        while( !toBeUsedMapClone.isEmpty() ) {
            DirectedGraphNode graam1Node = toBeUsedMapClone.keySet().iterator().next();
            Set<DirectedGraphNode> mappedGraam2Nodes = toBeUsedMapClone.get(graam1Node);
            toBeUsedMapClone.remove( graam1Node );
            // If there is exactly one node of graam2Nodes mapped to graam1Node, then go over all the currently created
            // possible node mappings and add this pair to them. Otherwise, for each of the mapped graam2nodes, create a
            // combination of graam2nodes

            List<Map<DirectedGraphNode, DirectedGraphNode>> toBecomeSoFarCreatedMapping = new ArrayList<>();

            mappedGraam2Nodes.forEach( graam2Node -> {
                Map<DirectedGraphNode, Set<DirectedGraphNode>> clonedToBeUsedMapClone = new HashMap<>( toBeUsedMapClone );
                // Removing the graam2Node from the rest of the remained node mappings
                clonedToBeUsedMapClone.keySet().forEach( graphNode -> clonedToBeUsedMapClone.get( graphNode ).remove( graam2Node ) );

                List<Map<DirectedGraphNode, DirectedGraphNode>> soFarCreatedMappingClone = new ArrayList<>( soFarCreatedMapping );
                if( soFarCreatedMappingClone.isEmpty() )
                    soFarCreatedMappingClone.add( new HashMap<>() );
                soFarCreatedMappingClone.forEach( map -> map.put( graam1Node, graam2Node ) );

                createMappingCombinaition( soFarCreatedMappingClone, clonedToBeUsedMapClone );
                toBecomeSoFarCreatedMapping.addAll( soFarCreatedMappingClone );
            } );
            soFarCreatedMapping.clear();
            soFarCreatedMapping.addAll( toBecomeSoFarCreatedMapping );
        }
    }

        // Generates all the permutations of two lists and then tries to find one permutation of each list that matches to the other
    static List<Map<DirectedGraphNode, DirectedGraphNode>> findAllPossibleNodeMappings(List<DirectedGraphNode> graam1Nodes, List<DirectedGraphNode> graam2Nodes){
        List<Map<DirectedGraphNode, DirectedGraphNode>> possibleMappings = new ArrayList<>();
        if( graam1Nodes.size() != graam2Nodes.size() )
            return possibleMappings;
        List<List<DirectedGraphNode>> graam2NodesPermutations = generatePerm( graam2Nodes );
        List<List<DirectedGraphNode>> graam1NodesPermutations = generatePerm( graam1Nodes );
        for( List<DirectedGraphNode> permutedGraam1Nodes: graam1NodesPermutations )
            for( List<DirectedGraphNode> permutedGraam2Nodes: graam2NodesPermutations ){
                if( permutedGraam1Nodes.size() != permutedGraam2Nodes.size() )
                    continue;
                boolean nodesAreSemanticallyTheSame = true;
                for( int i = 0; i < permutedGraam1Nodes.size(); i++ )
                    if( !areSemanticallyTheSame( permutedGraam1Nodes.get(i), permutedGraam2Nodes.get(i) ) ){
                        nodesAreSemanticallyTheSame = false;
                        break;
                    }
                if( !nodesAreSemanticallyTheSame )
                    continue;

                Map<DirectedGraphNode, DirectedGraphNode> mapping = new HashMap<>();
                for( int i = 0; i < permutedGraam1Nodes.size(); i++ )
                    mapping.put( permutedGraam1Nodes.get(i), permutedGraam2Nodes.get(i)  );
                possibleMappings.add( mapping );
            }
        return possibleMappings;
    }

    public static <E> List<List<E>> generatePerm(List<E> original) {
        List<E> clonedOriginal = new ArrayList<>( original );
        return generatePermInternal( clonedOriginal );
    }

    static <E> List<List<E>> generatePermInternal(List<E> original) {
        if (original.isEmpty()) {
            List<List<E>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }
        E firstElement = original.remove(0);
        List<List<E>> returnValue = new ArrayList<>();
        List<List<E>> permutations = generatePerm(original);
        for (List<E> smallerPermutated : permutations) {
            for (int index=0; index <= smallerPermutated.size(); index++) {
                List<E> temp = new ArrayList<>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    public static boolean areSemanticallyTheSame(DirectedGraphNode graamNode1, DirectedGraphNode graamNode2) {
        if (graamNode1 instanceof NonFrameworkBoundaryNode && graamNode2 instanceof NonFrameworkBoundaryNode) {
            return (((NonFrameworkBoundaryNode) graamNode1).getType().equals(NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE) &&
                    ((NonFrameworkBoundaryNode) graamNode2).getType().equals(NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE)) ||
                    (((NonFrameworkBoundaryNode) graamNode1).getType().equals(NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE) &&
                            ((NonFrameworkBoundaryNode) graamNode2).getType().equals(NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE));
        }

        if (graamNode1 instanceof FrameworkRelatedNode && graamNode2 instanceof FrameworkRelatedNode) {
            if (!((FrameworkRelatedNode) graamNode1).getFrameworkRelatedClass().equals(((FrameworkRelatedNode) graamNode2).getFrameworkRelatedClass()))
                return false;
            if (((FrameworkRelatedNode) graamNode1).isNewObjectNode() && ((FrameworkRelatedNode) graamNode2).isNewObjectNode()) {
                return true;
            }
            if (((FrameworkRelatedNode) graamNode1).isInitNode() && ((FrameworkRelatedNode) graamNode2).isInitNode()) {
                return areOverridenMethods(((FrameworkRelatedNode) graamNode1).getFrameworkRelatedMethod(), ((FrameworkRelatedNode) graamNode2).getFrameworkRelatedMethod());
            } else if (((FrameworkRelatedNode) graamNode1).isNormalMethodCall() && ((FrameworkRelatedNode) graamNode2).isNormalMethodCall()) {
                return ((FrameworkRelatedNode) graamNode1).getFrameworkRelatedMethod().equals(((FrameworkRelatedNode) graamNode2).getFrameworkRelatedMethod());
            }
        }
        return false;
    }

    static boolean areOverridenMethods(String method1, String method2) {
        String method1Name = method1.split("\\(")[0];
        String method2Name = method2.split("\\(")[0];

        String method1ReturnType = method1.split("\\)")[1];
        String method2ReturnType = method2.split("\\)")[1];

        return method1Name.equals(method2Name) && method1ReturnType.equals(method2ReturnType);
    }

    static boolean haveTheSameParents(GRAAM graam, DirectedGraphNode graamNode1, DirectedGraphNode graamNode2) {
        List<DirectedGraphNode> graamNode1ExplParedecessors = StreamSupport.stream(graam.getPredNodes(graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP).spliterator(),
                false).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode1ImplParedecessors = StreamSupport.stream(graam.getPredNodes(graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP).spliterator(),
                false).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode2ExplParedecessors = StreamSupport.stream(graam.getPredNodes(graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP).spliterator(),
                false).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode2ImplParedecessors = StreamSupport.stream(graam.getPredNodes(graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP).spliterator(),
                false).collect(Collectors.toList());

        return graamNode1ExplParedecessors.containsAll(graamNode2ExplParedecessors) &&
                graamNode2ExplParedecessors.containsAll(graamNode1ExplParedecessors) &&
                graamNode1ImplParedecessors.containsAll(graamNode2ImplParedecessors) &&
                graamNode2ImplParedecessors.containsAll(graamNode1ImplParedecessors);
    }

    public static List<GRAAM> loadGRAAMsFromSerializedFolder(String serializedGRAAMsFolder) throws IOException, ClassNotFoundException {
        List<GRAAM> loadedGRAAMs = new ArrayList<>();
        List<File> filesInFolder = Files.walk(Paths.get(serializedGRAAMsFolder))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        File parentFolder = new File(serializedGRAAMsFolder);

        for (File file : filesInFolder) {
            if (!file.getParentFile().getAbsolutePath().equals(parentFolder.getAbsolutePath()) || !file.getName().endsWith(SERIALIZED_GRAAM_EXTENSION))
                continue;
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            GRAAM retrievedGRAAM = (GRAAM) objectIn.readObject();
            objectIn.close();
            loadedGRAAMs.add(retrievedGRAAM);
        }

        return loadedGRAAMs;
    }

    public static void saveSerializedGRAAMs(List<GRAAM> graams, String serializedGRAAMsFolder) {
        Set<String> createdFiles = new HashSet<>();
        File folder = new File(serializedGRAAMsFolder);
        if (!folder.exists())
            folder.mkdir();

        graams.forEach(graam -> {
            String fileName = generateUniqueFileName(graam.getTitle(), createdFiles);
            createdFiles.add(fileName);

            String filePath = serializedGRAAMsFolder + File.separator + fileName + SERIALIZED_GRAAM_EXTENSION;
            FileOutputStream serializedGraspFile = null;
            try {
                serializedGraspFile = new FileOutputStream(filePath);
                ObjectOutputStream objectOut = new ObjectOutputStream(serializedGraspFile);
                objectOut.writeObject(graam);
                objectOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static void saveGRAAMsDotGraph(List<GRAAM> graams, String GRAAMsDotGraphFolder) {
        Set<String> createdFiles = new HashSet<>();
        File folder = new File(GRAAMsDotGraphFolder);
        if (!folder.exists())
            folder.mkdir();

        graams.forEach(graam -> {
            String fileName = generateUniqueFileName(graam.getTitle(), createdFiles);
            createdFiles.add(fileName);

            String filePath = GRAAMsDotGraphFolder + File.separator + fileName + DOT_GRAPH_EXTENSION;
            try {
                FileWriter fileWriter = new FileWriter( filePath );
                fileWriter.write( (new GRAAMVisualizer( graam )).dotOutput().toString() );
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }


    static String generateUniqueFileName(String suggestedFileName, Set<String> takenFileNames) {
        String generatedFileName = suggestedFileName;
        int fileNameCounter = 1;
        while (takenFileNames.contains(generatedFileName)) {
            generatedFileName =
                    (fileNameCounter == 1 ?
                            generatedFileName + "_1" :
                            generatedFileName.substring(0, generatedFileName.length() - (String.valueOf(fileNameCounter).length() + 1)) + "_" + fileNameCounter);
            fileNameCounter++;
        }
        return generatedFileName;
    }

    // If there is an explicit data dependency from node1 to node2, then, node1 is defining a data that is used by node2.
    public static Set<FrameworkRelatedNode> findDefNodes(GRAAM graam, FrameworkRelatedNode graamNode  ){
        Set<DirectedGraphNode> explicitPreds = StreamSupport.stream( graam.getPredNodes( graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false).collect(Collectors.toSet());
        Set<FrameworkRelatedNode> defNodes = new HashSet<>();
        /*Set<String> defTypes = new HashSet<>();
        if( !graamNode.isInitNode() )
            defTypes.add( findFrameworkClassType( graamNode ) );
        defTypes.addAll( findArgumentTypes( graamNode ) );
        explicitPreds.stream().filter( directedGraphNode -> directedGraphNode instanceof FrameworkRelatedNode &&
                defTypes.contains( findFrameworkClassType((FrameworkRelatedNode) directedGraphNode) ) ).
                spliterator().forEachRemaining( directedGraphNode -> defNodes.add((FrameworkRelatedNode) directedGraphNode) );*/

        explicitPreds.stream().filter( directedGraphNode -> directedGraphNode instanceof FrameworkRelatedNode ).
                forEach( directedGraphNode -> defNodes.add((FrameworkRelatedNode) directedGraphNode) );
        return defNodes;
    }

    //TODO: Returning the first compatible parent is an estimation! We need a more accurate way to find the exact reference object.
    public static FrameworkRelatedNode findObjectReferenceNode( GRAAM graam, FrameworkRelatedNode graamNode  ){
        if( graamNode.isStaticMethod() )
            return null;

        if( graamNode.isInitNode() )
            return null;

        String graamNodeClassType = findFrameworkClassType( graamNode );

        Set<FrameworkRelatedNode> defNodes = findDefNodes( graam, graamNode );
        // Check whether there is an instantiation pred which its class is the same as the class of graamNode. If there is one, that would be
        // considered as the reference node.
        for (FrameworkRelatedNode explicitPred : defNodes) {
            if( !explicitPred.isInitNode() )
                continue;
            String graamNodePredClassType = findFrameworkClassType( explicitPred );
//            String graamNodePredReturnType = findFrameworkMethodReturnType( explicitPredCast );

            if( graamNodePredClassType.equals( graamNodeClassType ) )
                return explicitPred;
        }

        for (FrameworkRelatedNode explicitPred : defNodes) {
            if( explicitPred.isInitNode() )
                continue;

            String graamNodePredReturnType = findFrameworkMethodReturnType( explicitPred );

            if( graamNodePredReturnType.equals( graamNodeClassType ) )
                return explicitPred;
        }

        for (FrameworkRelatedNode explicitPred : defNodes) {
            if( !explicitPred.isInitNode() )
                continue;
            String graamNodePredClassType = findFrameworkClassType( explicitPred );
//            String graamNodePredReturnType = findFrameworkMethodReturnType( explicitPredCast );

            if( graamNodePredClassType.equals( "[" + graamNodeClassType ) || graamNodePredClassType.equals( "L" + graamNodeClassType ) )
                return explicitPred;

/*
            if( (explicitPredCast.isInitNode()  && explicitPredCast.getFrameworkRelatedClass().equals( "[" + graamNode.getFrameworkRelatedClass() ) ) ||
                    (!explicitPredCast.isInitNode()  && findFrameworkMethodReturnType(explicitPredCast).equals( "L" + graamNode.getFrameworkRelatedClass() )) )
                return explicitPredCast;
*/
        }

        for (FrameworkRelatedNode explicitPred : defNodes) {
            if( !explicitPred.isInitNode() )
                continue;
            String graamNodePredClassType = findFrameworkClassType( explicitPred );
            String graamNodePredReturnType = findFrameworkMethodReturnType( explicitPred );

            if( COLLECTION_CLASS_NAMES.contains( graamNodePredClassType)  ||
                    (!explicitPred.isInitNode()  && COLLECTION_CLASS_NAMES.contains( graamNodePredReturnType) ) )
                return explicitPred;
        }

        return null;

    }

    static String findFrameworkMethodReturnType(FrameworkRelatedNode graamNode ){
        int index = graamNode.getFrameworkRelatedMethod().indexOf( ")" );
        String type = graamNode.getFrameworkRelatedMethod().substring( index + 1).replaceAll(";", "");
        if( type.startsWith("L") )
            type = type.substring(1);
        return type;
//        return graamNode.getFrameworkRelatedMethod().substring( index + 1).replaceAll(";", "");
    }
    static String findFrameworkClassType(FrameworkRelatedNode graamNode ){
        String type = graamNode.getFrameworkRelatedClass();
        if( type.startsWith("L") )
            type = type.substring( 1 );
        return type;
    }
    static List<String> findArgumentTypes(FrameworkRelatedNode graamNode ){
        int sIndex = graamNode.getFrameworkRelatedMethod().indexOf( "(" ) + 1;
        int eIndex = graamNode.getFrameworkRelatedMethod().indexOf( ")" );
        List<String> argTypes = new ArrayList<>();
        String[] types = graamNode.getFrameworkRelatedMethod().substring( sIndex, eIndex).split(";");
        for (String type : types) {
            if( type.length() == 0)
                continue;
            if( type.startsWith("L") )
                type = type.substring(1);
            argTypes.add( type );
        }
        return argTypes;
//        return graamNode.getFrameworkRelatedMethod().substring( index + 1).replaceAll(";", "");
    }
    public static void swapNodesContent( FrameworkRelatedNode frameworkRelatedNode1, FrameworkRelatedNode frameworkRelatedNode2 ){
        StatementRepresentation statementRepresentation1 = frameworkRelatedNode1.statementRepresentation;
        StatementRepresentation statementRepresentation2 = frameworkRelatedNode2.statementRepresentation;
        frameworkRelatedNode1.statementRepresentation = statementRepresentation2;
        frameworkRelatedNode2.statementRepresentation = statementRepresentation1;
    }
}
