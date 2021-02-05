package edu.rit.se.design.arcode.fspecminer.fspec;

import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.graam.NonFrameworkBoundaryNode;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class FSpecUtil {
    public static boolean areSemanticallyTheSame(FSpecNode fSpecNode, DirectedGraphNode graphNode){
        if( fSpecNode instanceof FSpecStartNode && graphNode instanceof NonFrameworkBoundaryNode &&
                ((NonFrameworkBoundaryNode)graphNode).getType().equals( NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE ) )
            return true;

        if( fSpecNode instanceof FSpecEndNode && graphNode instanceof NonFrameworkBoundaryNode &&
                ((NonFrameworkBoundaryNode)graphNode).getType().equals( NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE ) )
            return true;

        if( fSpecNode instanceof FSpecAPIInstantiationNode && graphNode instanceof FrameworkRelatedNode &&
                ((FrameworkRelatedNode) graphNode).isInitNode() &&
                ((FrameworkRelatedNode) graphNode).getFrameworkRelatedClass().equals( ((FSpecAPIInstantiationNode) fSpecNode).getFullClassName() ) &&
                areOverridenMethods( ((FrameworkRelatedNode) graphNode).getFrameworkRelatedMethod(),
                        ((FSpecAPIInstantiationNode) fSpecNode).getConstructorSignature() ))
            return true;

        if ( fSpecNode instanceof FSpecAPICallNode && graphNode instanceof FrameworkRelatedNode &&
                !((FrameworkRelatedNode) graphNode).isInitNode() &&
                ((FrameworkRelatedNode) graphNode).getFrameworkRelatedClass().equals( ((FSpecAPICallNode) fSpecNode).getFullClassName() ) &&
                ((FrameworkRelatedNode) graphNode).getFrameworkRelatedMethod().equals( ((FSpecAPICallNode) fSpecNode).getMethodSignature() )
        )
            return true;
        return false;
    }

    static boolean areOverridenMethods( String method1, String method2 ){
        String method1Name = method1.split("\\(")[0];
        String method2Name = method2.split("\\(")[0];

        String method1ReturnType = method1.split("\\)")[1];
        String method2ReturnType = method2.split("\\)")[1];

        return method1Name.equals( method2Name ) && method1ReturnType.equals( method2ReturnType );
    }

    public static FSpec loadFromFile( String filePath ) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filePath);
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        FSpec retrievedFSpec = (FSpec) objectIn.readObject();
        objectIn.close();
        return retrievedFSpec;
    }

    // Assumption: There are not two similar nodes in the FSpec with the same context
    public static Map<DirectedGraphNode, FSpecNode> findContextBasedNodeMapping(FSpec fSpec, GRAAM graam, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping) {
        Map<DirectedGraphNode, FSpecNode> mappedNodes = new HashMap<>();
        graam.iterator().forEachRemaining( graamNode -> {
            fSpec.iterator().forEachRemaining( fSpecNode -> {
                if( !mappedNodes.values().contains( fSpecNode ) ){
                    if( areContextBaseSemanticallyEqiuvalent(fSpec, fSpecNode, graam, graamNode, edgeTypeMapping) ) {
                        mappedNodes.put(graamNode, fSpecNode);
                        return;
                    }
                }
            } );
        } );

        return mappedNodes;

/*        FSpecNode fspecStartNode = fSpec.getRoot();
        DirectedGraphNode graamStartNode = graam.getStartNode();

        Map<DirectedGraphNode, FSpecNode> mappedNodes = new HashMap<>();
        findContextBasedNodeMapping( fSpec, graam, edgeTypeMapping, mappedNodes, fspecStartNode, graamStartNode );

        return mappedNodes;*/
    }

    static boolean areContextBaseSemanticallyEqiuvalent( FSpec fSpec, FSpecNode fSpecNode, GRAAM graam, DirectedGraphNode graamNode, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping ){
        if( !areSemanticallyTheSame(fSpecNode, graamNode)  )
            return false;

        List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false).collect(Collectors.toList());
        List<FSpecNode> fspecNodePreds = StreamSupport.stream( fSpec.getPredNodes( fSpecNode ).spliterator(), false).collect(Collectors.toList());

        if( graamNodePreds.size() != fspecNodePreds.size() )
            return false;

        // They are semantically the same and both have no parents
        if( graamNodePreds.size() == 0 )
            return true;

        // Collect all the possible node mappings for the preds that each graamNodePred is exactly mapped to one fspecNodePred
        List<Map<DirectedGraphNode, FSpecNode>> allPossibleNodeMappings =  StreamSupport.stream(
                findAllPossibleNodeMappings( fspecNodePreds, graamNodePreds ).spliterator(), false )
                .filter( nodeMapping -> nodeMapping.size() == graamNodePreds.size() ).collect(Collectors.toList());

        // Collect those node mappings that the edge type between them and the given graamNode/fSpecNode are equivalent
        allPossibleNodeMappings = StreamSupport.stream( allPossibleNodeMappings.spliterator(), false ).filter( nodeMap -> {
            for (DirectedGraphNode graamPredNode : nodeMap.keySet()) {
                FSpecNode fSpecPredNode = nodeMap.get( graamPredNode );
                for (GRAAMEdgeType graamEdgeType : edgeTypeMapping.keySet()) {
                    FSpecEdgeType fSpecEdgeType = edgeTypeMapping.get( graamEdgeType );
                    boolean fspecHasEdge = fSpec.hasEdge( fSpecPredNode, fSpecNode, fSpecEdgeType );
                    boolean graamHasEdge = graam.hasEdge(graamPredNode, graamNode, graamEdgeType);

                    if ( (fspecHasEdge && !graamHasEdge) || (graamHasEdge && !fspecHasEdge) )
                        return false;
                }
            }
            return true;
        } ).collect(Collectors.toList());

        // Iterate over all the found valid mappings for the predecessors. If either of the found mappings
        // result in a context-base semantically equivalency, then return true
        for (Map<DirectedGraphNode, FSpecNode> possibleNodeMapping : allPossibleNodeMappings) {
            boolean areSemanticallyTheSameForThisMapping = true;
            for (DirectedGraphNode graamPredNode : possibleNodeMapping.keySet()) {
                FSpecNode fSpecPredNode = possibleNodeMapping.get( graamPredNode );
                areSemanticallyTheSameForThisMapping = areSemanticallyTheSameForThisMapping &&
                        areContextBaseSemanticallyEqiuvalent( fSpec, fSpecPredNode, graam, graamPredNode, edgeTypeMapping );
            }
            if( areSemanticallyTheSameForThisMapping )
                return true;
        }

        return false;
/*
        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
        for (GRAAMEdgeType graamEdgeType : edgeTypeMapping.keySet()) {
            FSpecEdgeType fSpecEdgeType = edgeTypeMapping.get( graamEdgeType );
            List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode, graamEdgeType ).spliterator(), false).collect(Collectors.toList());
            List<FSpecNode> fspecNodePreds = StreamSupport.stream( fSpec.getPredNodes( fSpecNode, fSpecEdgeType ).spliterator(), false).collect(Collectors.toList());
            if( graamNodePreds.size() != fspecNodePreds.size() )
                return false;
            for( DirectedGraphNode graamNodePred : graamNodePreds ){
                List<FSpecNode> semanticallyTheSameNodes = StreamSupport.stream( fspecNodePreds.spliterator(), false ).filter( fSpecNodePred -> areSemanticallyTheSame( fSpecNodePred, graamNodePred ) ).collect(Collectors.toList());
                if (semanticallyTheSameNodes.size() == 0)
                    return false;
                //FIXME: In the case of pod_serverApp.jar, two predecessors of the END_NODE are the same APIs. We need to have a fork that computes
                // all the possible combination of mappings here
                FSpecNode semanticallyEquivalentFSpecNode = semanticallyTheSameNodes.get(0);
                nodeMapping.put( graamNodePred, semanticallyEquivalentFSpecNode );
            }
        }



        boolean areSemanticallyTheSame = true;
        for( DirectedGraphNode graamNodePred: nodeMapping.keySet() )
            areSemanticallyTheSame = areSemanticallyTheSame && areContextBaseSemanticallyEqiuvalent( fSpec, nodeMapping.get( graamNodePred ), graam, graamNodePred, edgeTypeMapping );

        return areSemanticallyTheSame;*/
    }

 /*       // So, start from the root of FSpec and the GRAAM and go downward.
    static List<Map<DirectedGraphNode, FSpecNode>> findAllPossibleNodeMappingList(List<DirectedGraphNode> graamNodes, List<FSpecNode> fSpecNodes){
        List<Map<DirectedGraphNode, FSpecNode>> nodeBaseNodeMappings = new ArrayList<>();
        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
        graamNodes.forEach( graamNode -> fSpecNodes.forEach( fSpecNode -> {
            if( nodeMapping.values().contains( fSpecNode ) || !areSemanticallyTheSame( fSpecNode, graamNode ))
                return;
            nodeMapping.put( graamNode, fSpecNode );
        } ) );
        return nodeBaseNodeMappings;
    }*/

    // Finds mapped graamNodes and FSpecNodes that are context-based (predecessors) equivalent in a BFS manner.
    static void findContextBasedNodeMapping(FSpec fSpec, GRAAM graam, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping, Map<DirectedGraphNode, FSpecNode> mappedNodes, FSpecNode fSpecNode, DirectedGraphNode graamNode) {
        List<Pair<DirectedGraphNode, FSpecNode>> toBeVisitedMappedNodes = new ArrayList<>();
        toBeVisitedMappedNodes.add( new ImmutablePair<>(graamNode, fSpecNode) );
        while( !toBeVisitedMappedNodes.isEmpty() ) {
            Pair<DirectedGraphNode, FSpecNode> mappedNode = toBeVisitedMappedNodes.remove(0);
            DirectedGraphNode mappedGraamNode = mappedNode.getKey();
            FSpecNode mappedFSpecNode = mappedNode.getValue();

            mappedNodes.put( mappedGraamNode, mappedFSpecNode );

            graam.getSuccNodes(mappedGraamNode).forEach(mappedGraamNodeSucc -> {
                //Find all the edge types between graamNode and its successor
                Set<GRAAMEdgeType> mappedGraamNodeSuccConnectionEdgeTypes = StreamSupport.stream(edgeTypeMapping.keySet().spliterator(), false).filter(graamEdgeType -> graam.hasEdge(mappedGraamNode, mappedGraamNodeSucc, graamEdgeType)).collect(Collectors.toSet());
                Set<FSpecEdgeType> mappedFSpecNodeSuccConnectionEdgeTypes = new HashSet<>();
                mappedGraamNodeSuccConnectionEdgeTypes.forEach(graamEdgeType -> mappedFSpecNodeSuccConnectionEdgeTypes.add(edgeTypeMapping.get(graamEdgeType)));

                // Find a successor of fSpecNode (fSpecNodeSucc) that:
                //  (i) is semantically equivalent to mappedGraamNodeSucc and
                //  (ii) is connected to fSpecNode with all the corresponding edge types that mappedGraamNodeSucc is connected to graamNode and
                // (iii) has semantically equivalent parents (fSpecNodeSuccPreds) as mappedGraamNodeSucc parents (graamNodeSuccPreds) that are connected with the same
                //      corresponding edges
                List<FSpecNode> matchedFSpecNodes = StreamSupport.stream(fSpec.getSuccNodes(mappedFSpecNode).spliterator(), false).filter(
                        mappedFSpecNodeSucc -> areSemanticallyTheSame(mappedFSpecNodeSucc, mappedGraamNodeSucc) &&
                                isConnectedTo(fSpec, mappedFSpecNodeSucc, mappedFSpecNode, mappedFSpecNodeSuccConnectionEdgeTypes) &&
                                hasTheSemanticallyEquivalentPreds(fSpec, mappedFSpecNodeSucc, graam, mappedGraamNodeSucc, edgeTypeMapping)
                ).collect(Collectors.toList());

                if (matchedFSpecNodes.size() > 0)
                    toBeVisitedMappedNodes.add(new ImmutablePair<>( mappedGraamNodeSucc, matchedFSpecNodes.get(0) ));
            });
        }
//        toBeVisitedMappedNodes.keySet().forEach( mappedGraamNode ->
//                findContextBasedNodeMapping( fSpec, graam, edgeTypeMapping, mappedNodes, toBeVisitedMappedNodes.get(mappedGraamNode), mappedGraamNode )
//        );
    }

    static boolean isConnectedTo( FSpec fSpec, FSpecNode fSpecNodeSucc, FSpecNode fSpecNode, Set<FSpecEdgeType> connectionEdgeTypes ){
        for( FSpecEdgeType fSpecEdgeType : connectionEdgeTypes )
            if( !fSpec.hasEdge( fSpecNode, fSpecNodeSucc, fSpecEdgeType ) )
                return false;
        return true;
    }

    static boolean hasTheSemanticallyEquivalentPreds( FSpec fSpec, FSpecNode fSpecNode, GRAAM graam, DirectedGraphNode graamNode, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping ){
        List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        List<FSpecNode> fspecNodePreds = StreamSupport.stream( fSpec.getPredNodes( fSpecNode ).spliterator(), false ).collect(Collectors.toList());

        if( graamNodePreds.size() != fspecNodePreds.size() )
            return false;

        for(DirectedGraphNode graamNodePred: graamNodePreds){
            Set<GRAAMEdgeType> mappedGraamNodePredConnectionEdgeTypes = StreamSupport.stream(
                    edgeTypeMapping.keySet().spliterator(), false)
                    .filter(graamEdgeType -> graam.hasEdge(graamNodePred, graamNode, graamEdgeType)).collect(Collectors.toSet());
            Set<FSpecEdgeType> mappedFSpecNodePredConnectionEdgeTypes = new HashSet<>();
            mappedGraamNodePredConnectionEdgeTypes.forEach(graamEdgeType -> mappedFSpecNodePredConnectionEdgeTypes.add(edgeTypeMapping.get(graamEdgeType)));

            List<FSpecNode> matchedFSpecNodePreds = StreamSupport.stream(fspecNodePreds.spliterator(), false).filter(
                    fspecNodePred -> areSemanticallyTheSame(fspecNodePred, graamNodePred) &&
                            isConnectedTo(fSpec, fSpecNode, fspecNodePred, mappedFSpecNodePredConnectionEdgeTypes)
            ).collect(Collectors.toList());
            if( matchedFSpecNodePreds.isEmpty() )
                return false;

            fspecNodePreds.remove( matchedFSpecNodePreds.get(0) );
        }

        return true;
    }

/*
     public static Map<DirectedGraphNode, FSpecNode> findContextBasedNodeMappingOld(FSpec fSpec, GRAAM graam, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping) {
        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
        graam.iterator().forEachRemaining( graamNode ->  {
            fSpec.iterator().forEachRemaining( fSpecNode -> {
                if( areContextBasedTheSame( graam, graamNode, fSpec, fSpecNode, edgeTypeMapping ) ){
                    nodeMapping.put( graamNode, fSpecNode );
                }
            } );
        }  );
        return nodeMapping;

    }
*/
/*    static boolean areContextBasedTheSame( GRAAM graam, DirectedGraphNode graamNode, FSpec fSpec, FSpecNode fSpecNode, Map<GRAAMEdgeType, FSpecEdgeType> edgeTypeMapping ){
        if( graamNode == null && fSpecNode == null )
            return true;
        if( (graamNode == null && fSpecNode != null) || (graamNode != null && fSpecNode == null) )
            return false;
        if( !FSpecUtil.areSemanticallyTheSame( fSpecNode, graamNode ) )
            return false;

        Map<GRAAMEdgeType, List<DirectedGraphNode>> graamNodeParedecessorsMapping = new HashMap<>();
        Map<FSpecEdgeType, List<FSpecNode>> fspecNodeParedecessorsMapping = new HashMap<>();

        int predecessorCounter = 0;
        for( GRAAMEdgeType graamEdgeType : edgeTypeMapping.keySet()){
            List<DirectedGraphNode> graamNodePredList =  StreamSupport.stream( graam.getPredNodes( graamNode, graamEdgeType ).spliterator(),
                    false ).collect(Collectors.toList());
            graamNodeParedecessorsMapping.put( graamEdgeType, graamNodePredList );
            predecessorCounter += graamNodePredList.size();

            FSpecEdgeType fspecMappedEdgeType = edgeTypeMapping.get( graamEdgeType );
            List<FSpecNode> fspecNodePredList = StreamSupport.stream( fSpec.getPredNodes( fSpecNode, fspecMappedEdgeType ).spliterator(),
                    false ).collect(Collectors.toList());
            fspecNodeParedecessorsMapping.put( fspecMappedEdgeType, fspecNodePredList );
            predecessorCounter += fspecNodePredList.size();
        }

*//*        List<DirectedGraphNode> graamNodeParedecessors = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(),
                false ).collect(Collectors.toList());
        List<FSpecNode> fSpecNodePredecessors = StreamSupport.stream( fSpec.getPredNodes( fSpecNode ).spliterator(),
                false ).collect(Collectors.toList());*//*

        if( predecessorCounter == 0 )
            return true;

        List<List<Map<DirectedGraphNode, FSpecNode>>> allPossibleNodeMappingsList= new ArrayList<>();
        edgeTypeMapping.keySet().forEach( graamEdgeType -> {
            FSpecEdgeType fSpecEdgeType = edgeTypeMapping.get( graamEdgeType );
            List<Map<DirectedGraphNode, FSpecNode>> allPossibleNodeMappings = findAllPossibleNodeMappings( fspecNodeParedecessorsMapping.get( fSpecEdgeType ), graamNodeParedecessorsMapping.get( graamEdgeType ) );
            if( allPossibleNodeMappings.size() > 0 && allPossibleNodeMappings.get(0).size() > 0)
                allPossibleNodeMappingsList.add( allPossibleNodeMappings );
        } );
//        List<Map<DirectedGraphNode, FSpecNode>> allPossibleNodeMappings =
//                findAllPossibleNodeMappings( fSpecNodePredecessors, graamNodeParedecessors );

        // Finding the intersection of all lists
        // FIXME: there might be a node which is only connected to its parents via on edge type. In that case intersecting lists from EXPLICIT_DATA_DEPT and IMPLICIT_DATA_DEPT might exclude those nodes
        List<Map<DirectedGraphNode, FSpecNode>> allPossibleNodeMappings = new ArrayList<>();
        for( int i = 0; i < allPossibleNodeMappingsList.size(); i++ ){
            if( i == 0 )
                allPossibleNodeMappings.addAll( allPossibleNodeMappingsList.get(i) );
            else
                allPossibleNodeMappings.retainAll( allPossibleNodeMappingsList.get(i) );
        }


        for (Map<DirectedGraphNode, FSpecNode> aPossibleNodeMapping : allPossibleNodeMappings) {
            boolean areContextBasedTheSame = true;
            for (DirectedGraphNode candidateMappedGraamNode : aPossibleNodeMapping.keySet()) {
                if( !areContextBasedTheSame( graam, candidateMappedGraamNode, fSpec, aPossibleNodeMapping.get( candidateMappedGraamNode ), edgeTypeMapping) ){
                    areContextBasedTheSame = false;
                    break;
                }
            }
            if( areContextBasedTheSame )
                return true;
        }
        return false;
    }*/

    // Generates all the permutations of two lists and then tries to find one permutation of each list that matches to the other
    static List<Map<DirectedGraphNode, FSpecNode>> findAllPossibleNodeMappings(List<FSpecNode> fSpecNodes, List<DirectedGraphNode> graamNodes){
        List<Map<DirectedGraphNode, FSpecNode>> possibleMappings = new ArrayList<>();
        if( fSpecNodes.size() != graamNodes.size() )
            return possibleMappings;
        List<List<DirectedGraphNode>> graamNodesPermutations = generatePerm( graamNodes );
        List<List<FSpecNode>> fSpecNodesPermutations = generatePerm( fSpecNodes );
        for( List<DirectedGraphNode> permutedGraamNodes: graamNodesPermutations )
            for( List<FSpecNode> permutedFSpecNodes: fSpecNodesPermutations ){
                if( permutedFSpecNodes.size() != permutedGraamNodes.size() )
                    continue;
                boolean nodesAreSemanticallyTheSame = true;
                for( int i = 0; i < permutedFSpecNodes.size(); i++ )
                    if( !FSpecUtil.areSemanticallyTheSame( permutedFSpecNodes.get(i), permutedGraamNodes.get(i) ) ){
                        nodesAreSemanticallyTheSame = false;
                        break;
                    }
                if( !nodesAreSemanticallyTheSame )
                    continue;

                Map<DirectedGraphNode, FSpecNode> mapping = new HashMap<>();
                for( int i = 0; i < permutedFSpecNodes.size(); i++ )
                    mapping.put( permutedGraamNodes.get(i), permutedFSpecNodes.get(i)  );
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

}
