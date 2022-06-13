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

package edu.rit.se.design.arcode.fspecminer.fspec;

import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecBuilder {

    public static FSpec buildFSpec(String framework, List<GRAAM> graamList){
        FSpec fSpec = new FSpec( framework );

        Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping = new HashMap<>();
        edgeTypeMapping.put( GRAAMEdgeType.EXPLICIT_DATA_DEP, FSpecEdgeType.EXPLICIT_DATA_DEP );
        edgeTypeMapping.put( GRAAMEdgeType.IMPLICIT_DATA_DEP, FSpecEdgeType.IMPLICIT_DATA_DEP );
        FSpecBuildReport fSpecBuildReport = new FSpecBuildReport();
//        graamList = StreamSupport.stream(graamList.spliterator(), false).sorted( (o2, o1) -> o1.getNodeSet().size()>o2.getNodeSet().size()?1:(o1.getNodeSet().size()==o2.getNodeSet().size()?0:-1) ).collect(Collectors.toList());
        graamList.forEach( graam -> {
            mergeGRAAMWithFSpec( fSpec, graam, edgeTypeMapping);
            fSpecBuildReport.addLog( fSpec, graam );
        } );
        return fSpec;
    }

    public static void mergeGRAAMWithFSpec( FSpec fSpec, GRAAM graam, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping ){

        Map<DirectedGraphNode, FSpecNode> nodeMapping = FSpecUtil.findContextBasedNodeMapping( fSpec, graam, edgeTypeMapping );
        mergeGRAAMWithFSpec( nodeMapping, new HashSet<>(), fSpec,
                graam, graam.getRoot(), edgeTypeMapping );
    }

    static void mergeGRAAMWithFSpec(Map<DirectedGraphNode, FSpecNode> nodeMap, Set<DirectedGraphNode> visitedGRAAMNodes, FSpec fSpec,
                                    GRAAM graam, DirectedGraphNode graamNode, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping){
        edgeTypeMapping.keySet().forEach( graamEdgeType -> {
            FSpecEdgeType fSpecEdgeType = edgeTypeMapping.get( graamEdgeType );
            graam.getSuccNodes( graamNode, graamEdgeType ).forEach( graamNodeSucc -> {
                // Check to see if there is a corresponding node in the FSpec to graamNodeSucc. If so, then, add the
                // frequency of the corresponding edge or establish a new edge. Otherwise, create a new FSpecNode and establish
                // new edge.
                if( nodeMap.get( graamNodeSucc ) != null ){
                    // If there is an edge, increment the frequency by 1
                    if( fSpec.hasEdge( nodeMap.get( graamNode ), nodeMap.get( graamNodeSucc ), fSpecEdgeType ) )
                        fSpec.getEdge( nodeMap.get( graamNode ), nodeMap.get( graamNodeSucc ), fSpecEdgeType ).addFrequencyBy( 1, graam.getTitle() );
                    else
                    // else create an edge with frequency of 1
                        fSpec.addEdge( nodeMap.get( graamNode ), nodeMap.get( graamNodeSucc ), fSpecEdgeType , new FSpecEdge(graam.getTitle()) );

                    // Merge methodContextInfo for the current FSpecNode and the mapped GraamNode
                    if( nodeMap.get( graamNodeSucc ) instanceof FSpecAPINode ) {
                        ((FSpecAPINode) nodeMap.get(graamNodeSucc)).getMethodContextInfo().merge(((FrameworkRelatedNode)graamNodeSucc).getMethodContextInfo() );
                    }
                }
                else{
                    // Add the node with frequency of 1
                    FSpecNode fSpecNewNode = createNode( graamNodeSucc );
                    FSpecEdge fSpecNewEdge = new FSpecEdge( graam.getTitle() );
                    fSpec.addNode( fSpecNewNode );
                    fSpec.addEdge( nodeMap.get( graamNode ), fSpecNewNode, fSpecEdgeType, fSpecNewEdge );
                    nodeMap.put( graamNodeSucc, fSpecNewNode );
                }
            } );
        } );

        visitedGRAAMNodes.add( graamNode );

        graam.getSuccNodes(graamNode).forEach(graamNodeSucc -> {
            if (!visitedGRAAMNodes.contains(graamNodeSucc))
                mergeGRAAMWithFSpec(nodeMap, visitedGRAAMNodes, fSpec, graam, graamNodeSucc, edgeTypeMapping);
        });
    }

    static FSpecNode createNode( DirectedGraphNode graamNode ){
        FSpecNode fSpecNode = null;
        if( graamNode instanceof FrameworkRelatedNode ){
            fSpecNode = toFSpecAPINode((FrameworkRelatedNode) graamNode);
        }
        else if( graamNode instanceof NonFrameworkBoundaryNode && ((NonFrameworkBoundaryNode) graamNode).getType().equals(
                NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE ) ){
            fSpecNode = new FSpecStartNode();
        }
        else if( graamNode instanceof NonFrameworkBoundaryNode && ((NonFrameworkBoundaryNode) graamNode).getType().equals(
                NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE ) ){
            fSpecNode = new FSpecEndNode();
        }
        return fSpecNode;
    }

    static FSpecAPINode toFSpecAPINode(FrameworkRelatedNode graamNode ){
        FSpecAPINode fSpecAPINode = null;
        if( graamNode.isInitNode() )
            fSpecAPINode = new FSpecAPIInstantiationNode( graamNode.getFrameworkRelatedClass(), graamNode.getFrameworkRelatedMethod(),
                    graamNode.isAbstractOrInterfaceConstructorNode(), graamNode.isPublicMethod(), graamNode.getMethodContextInfo() );
        else
            fSpecAPINode = new FSpecAPICallNode( graamNode.getFrameworkRelatedClass(), graamNode.getFrameworkRelatedMethod(), graamNode.isStaticMethod(), graamNode.isPublicMethod(), graamNode.getMethodContextInfo() );
        return fSpecAPINode;
    }



//
//        //FIXME Needs some changes. Cyrrently, it does not consider all the predecessors as the prerequisite to find matches.
//
//    static Map<DirectedGraphNode, FSpecNode> findContextBasedNodeMapping(FSpec fSpec, FSpecEdgeType fSpecEdgeType, GRAAM graam,
//                                                                         GRAAMEdgeType graamEdgeType){
//        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
//        FSpecNode fSpecNode = fSpec.getRoot();
//        DirectedGraphNode graamNode = graam.getRoot();
//        findContextBasedNodeMapping( nodeMapping, fSpec, fSpecEdgeType, fSpecNode, new HashSet<>(), graam, graamEdgeType, graamNode, new HashSet<>()  );
//        return nodeMapping;
//    }
//
//    static void findContextBasedNodeMapping(Map<DirectedGraphNode, FSpecNode>  nodeMapping, FSpec fSpec, FSpecEdgeType fSpecEdgeType,
//                                            FSpecNode fSpecNode, Set<FSpecNode> visitedFSpecNodes,
//                                            GRAAM graam, GRAAMEdgeType graamEdgeType, DirectedGraphNode graamNode,
//                                            Set<DirectedGraphNode> visitedGraamNodes ){
//        if( visitedFSpecNodes.contains( fSpecNode ) || visitedGraamNodes.contains(graamNode) )
//            return;
//
//        visitedFSpecNodes.add( fSpecNode );
//        visitedGraamNodes.add( graamNode );
//
///*
//        if( !areSemanticallyTheSame( fSpecNode, graamNode ) )
//            return;
//*/
//
///*
//        // investigating whether predecessors of fSpecNode and graamNode are mapped or not
//        List<DirectedGraphNode> graamNodeParedecessors = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
//        List<FSpecNode> fSpecNodePredecessors = StreamSupport.stream( fSpec.getPredNodes( fSpecNode ).spliterator(), false ).collect(Collectors.toList());
//        Map<DirectedGraphNode, FSpecNode> predecessorsNodeMapping = findNodeMapping( graamNodeParedecessors,
//                fSpecNodePredecessors );
//        if( !(predecessorsNodeMapping.keySet().containsAll( graamNodeParedecessors ) &&
//                graamNodeParedecessors.containsAll( predecessorsNodeMapping.keySet() ) &&
//                predecessorsNodeMapping.values().containsAll( fSpecNodePredecessors ) &&
//                fSpecNodePredecessors.containsAll( predecessorsNodeMapping.values() )
//        ) )
//            return;
//*/
//
//        nodeMapping.put( graamNode, fSpecNode );
//
//        List<Map<DirectedGraphNode, FSpecNode>> nodeBasedNodeMappings = findNodeBasedNodeMapping( graam.getSuccNodes(graamNode, graamEdgeType),
//                fSpec.getSuccNodes( fSpecNode, fSpecEdgeType ) );
//        for( Map<DirectedGraphNode, FSpecNode> nodeBaseNodeMapping: nodeBasedNodeMappings )
//            if( isContextBasedNodeMapping( nodeBaseNodeMapping ) ){
//                nodeBaseNodeMapping.keySet().forEach( mappedGraamSuccNode -> findContextBasedNodeMapping( nodeMapping, fSpec, fSpecEdgeType,
//                        nodeBaseNodeMapping.get( mappedGraamSuccNode ), visitedFSpecNodes, graam, graamEdgeType, mappedGraamSuccNode, visitedGraamNodes) );
//                break;
//            }
//    }
//
//    static boolean isContextBasedNodeMapping( Map<DirectedGraphNode, FSpecNode> nodeMapping ){
//        return false;
//    }
//
//    // TODO: In addition to similarity of the nodes themselves, their parents should be the same as well.
//    // So, start from the root of FSpec and the GRAAM and go downward.
//    static List<Map<DirectedGraphNode, FSpecNode>> findNodeBasedNodeMapping(Iterable<DirectedGraphNode> graamNodes, Iterable<FSpecNode> fSpecNodes){
//        List<Map<DirectedGraphNode, FSpecNode>> nodeBaseNodeMappings = new ArrayList<>();
//        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
//        graamNodes.forEach( graamNode -> fSpecNodes.forEach( fSpecNode -> {
//            if( nodeMapping.values().contains( fSpecNode ) || !areSemanticallyTheSame( fSpecNode, graamNode ))
//                return;
//            nodeMapping.put( graamNode, fSpecNode );
//        } ) );
//        return nodeBaseNodeMappings;
//    }



}
