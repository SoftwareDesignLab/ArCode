package edu.rit.se.design.fspec2recom;

import edu.rit.se.design.specminer.graam.*;
import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class SwappedAPITestGenerator {
    static List<GRAAMEdgeType> toBeConsideredEdgeTypes = new ArrayList<>();
    static {
        toBeConsideredEdgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP );
//        toBeConsideredEdgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP );

    }

    public static Map<Integer, List<GRAAM>> generateTestCases(GRAAM originalGraam, int maximumSwappedAPI){
        Map<Integer, List<GRAAM>> generatedTestCases = new HashMap<>();

        List<GRAAM> lastSeed = new ArrayList<>();
        lastSeed.add( GRAAMBuilder.cloneFromScratch( originalGraam ).getKey() );
        int lastSwappedAPINumber = 0;

        while( !lastSeed.isEmpty() && lastSwappedAPINumber < maximumSwappedAPI ){
            generatedTestCases.put( lastSwappedAPINumber, new ArrayList<>( lastSeed ) );
            List<GRAAM> newSeed = new ArrayList<>();
            lastSeed.forEach( graam -> {
                newSeed.addAll( generateValidSwapTwoAPIGRAAMs( graam/*, toBeConsideredEdgeTypes*/ ) );
            } );
            lastSeed = findDistinctGRAAMs( newSeed );
            lastSwappedAPINumber++;
        }

        return generatedTestCases;
    }

    static List<GRAAM> findDistinctGRAAMs( List<GRAAM> graamList ){
        List<GRAAM> distinctGRAAMs = new ArrayList<>();
        graamList.forEach( graam -> {
            boolean isDuplicated = false;
            for (GRAAM distinctGRAAM : distinctGRAAMs) {
                if( graam.getNodeSet().size() != distinctGRAAM.getNodeSet().size() )
                    continue;

                Map<DirectedGraphNode, DirectedGraphNode> nodeMapping = GRAAMBuilder.findContextBasedNodeMapping( graam, distinctGRAAM );
                if( nodeMapping.keySet().size() == graam.getNodeSet().size() ) {
                    isDuplicated = true;
                    break;
                }
            }
            if( !isDuplicated )
                distinctGRAAMs.add( graam );
        } );
        return distinctGRAAMs;
    }

    static List<GRAAM> generateValidSwapTwoAPIGRAAMs(GRAAM graam ){
        List<GRAAM> allSwapTwoAPIsGRAAMs = new ArrayList<>();
        Set<Pair<DirectedGraphNode, DirectedGraphNode>> swappableNodes = findSwappableNodes( graam );
        swappableNodes.forEach( graamNodePair -> {
            Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGRAAMMap = GRAAMBuilder.cloneFromScratch( graam );
            GRAAM clonedGRAAM = clonedGRAAMMap.getKey();
            swapNodesAndRepairGraam( clonedGRAAM, graamNodePair, clonedGRAAMMap.getValue() );
            allSwapTwoAPIsGRAAMs.add( clonedGRAAM );
        } );
        return allSwapTwoAPIsGRAAMs;
    }

    static void swapNodesAndRepairGraam(GRAAM graam, Pair<DirectedGraphNode, DirectedGraphNode> graamNodePair, Map<DirectedGraphNode, DirectedGraphNode> clonedGraamNodeMapping ){
//        FrameworkRelatedNode frameworkRelatedNode1 = (FrameworkRelatedNode) clonedGraamNodeMapping.get(graamNodePair.getLeft());
//        FrameworkRelatedNode frameworkRelatedNode2 = (FrameworkRelatedNode) clonedGraamNodeMapping.get(graamNodePair.getRight());
//
//
//
//        GRAAMBuilder.swapNodesContent( frameworkRelatedNode1, frameworkRelatedNode2 );
        DirectedGraphNode graamNode1 = graamNodePair.getKey();
        DirectedGraphNode graamNode2 = graamNodePair.getValue();

        List<DirectedGraphNode> graamNode1ExpPreds = StreamSupport.stream( graam.getPredNodes( graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode1ExpSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode1ImpPreds = StreamSupport.stream( graam.getPredNodes( graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode1ImpSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());

        List<DirectedGraphNode> graamNode2ExpPreds = StreamSupport.stream( graam.getPredNodes( graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode2ExpSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode2ImpPreds = StreamSupport.stream( graam.getPredNodes( graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNode2ImpSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());

        graam.removeNodeAndEdges( graamNode1 );
        graam.removeNodeAndEdges( graamNode2 );

        graam.addNode( graamNode1 );
        graam.addNode( graamNode2 );

/*        graamNode1ExpPreds.forEach( graamNode1Pred -> {
            if( graam.hasEdge( graamNode1Pred, graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode1Pred, graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode1ExpSuccs.forEach( graamNode1Succ -> {
            if( graam.hasEdge( graamNode1, graamNode1Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode1, graamNode1Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode1ImpPreds.forEach( graamNode1Pred -> {
            if( graam.hasEdge( graamNode1Pred, graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode1Pred, graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );
        graamNode1ImpSuccs.forEach( graamNode1Succ -> {
            if( graam.hasEdge( graamNode1, graamNode1Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode1, graamNode1Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );

        graamNode2ExpPreds.forEach( graamNode2Pred -> {
            if( graam.hasEdge( graamNode2Pred, graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode2Pred, graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode2ExpSuccs.forEach( graamNode2Succ -> {
            if( graam.hasEdge( graamNode2, graamNode2Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
            graam.removeEdge( graamNode2, graamNode2Succ, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode2ImpPreds.forEach( graamNode2Pred -> {
            if( graam.hasEdge( graamNode2Pred, graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode2Pred, graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );
        graamNode2ImpSuccs.forEach( graamNode2Succ -> {
            if( graam.hasEdge( graamNode2, graamNode2Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.removeEdge( graamNode2, graamNode2Succ, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );*/


        graamNode1ExpPreds.forEach( graamNode1Pred -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode1Pred;
            if( graamNode1Pred.equals( graamNode2 ) )
                toBeEstablishedToEdge = graamNode1;
            if( !graam.hasEdge( toBeEstablishedToEdge, graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.addEdge( toBeEstablishedToEdge, graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode1ExpSuccs.forEach( graamNode1Succ -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode1Succ;
            if( graamNode1Succ.equals( graamNode2 ) )
                toBeEstablishedToEdge = graamNode1;
            if( !graam.hasEdge( graamNode2, toBeEstablishedToEdge, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.addEdge( graamNode2, toBeEstablishedToEdge, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode1ImpPreds.forEach( graamNode1Pred -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode1Pred;
            if( graamNode1Pred.equals( graamNode2 ) )
                toBeEstablishedToEdge = graamNode1;
            if( !graam.hasEdge( toBeEstablishedToEdge, graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.addEdge( toBeEstablishedToEdge, graamNode2, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );
        graamNode1ImpSuccs.forEach( graamNode1Succ -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode1Succ;
            if( graamNode1Succ.equals( graamNode2 ) )
                toBeEstablishedToEdge = graamNode1;
            if( !graam.hasEdge( graamNode2, toBeEstablishedToEdge, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.addEdge( graamNode2, toBeEstablishedToEdge, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );

        graamNode2ExpPreds.forEach( graamNode2Pred -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode2Pred;
            if( graamNode2Pred.equals( graamNode1 ) )
                toBeEstablishedToEdge = graamNode2;
            if( !graam.hasEdge( toBeEstablishedToEdge, graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.addEdge( toBeEstablishedToEdge, graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode2ExpSuccs.forEach( graamNode2Succ -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode2Succ;
            if( graamNode2Succ.equals( graamNode1 ) )
                toBeEstablishedToEdge = graamNode2;
            if( !graam.hasEdge( graamNode1, toBeEstablishedToEdge, GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
                graam.addEdge( graamNode1, toBeEstablishedToEdge, GRAAMEdgeType.EXPLICIT_DATA_DEP );
        } );
        graamNode2ImpPreds.forEach( graamNode2Pred -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode2Pred;
            if( graamNode2Pred.equals( graamNode1 ) )
                toBeEstablishedToEdge = graamNode2;
            if( !graam.hasEdge( toBeEstablishedToEdge, graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.addEdge( toBeEstablishedToEdge, graamNode1, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );
        graamNode2ImpSuccs.forEach( graamNode2Succ -> {
            DirectedGraphNode toBeEstablishedToEdge = graamNode2Succ;
            if( graamNode2Succ.equals( graamNode1 ) )
                toBeEstablishedToEdge = graamNode2;
            if( !graam.hasEdge( graamNode1, toBeEstablishedToEdge, GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
                graam.addEdge( graamNode1, toBeEstablishedToEdge, GRAAMEdgeType.IMPLICIT_DATA_DEP );
        } );

    }

    static Set<DirectedGraphNode> removePredEdges( GRAAM graam, DirectedGraphNode graamNode, GRAAMEdgeType graamEdgeType ){
        Set<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode, graamEdgeType ).spliterator(), false ).collect(Collectors.toSet());
        graamNodePreds.forEach( graamNodePred -> {
            if( graam.hasEdge( graamNodePred, graamNode, graamEdgeType ) )
                graam.removeEdge( graamNodePred, graamNode, graamEdgeType );
        } );
        return graamNodePreds;
    }

    static Set<DirectedGraphNode> removeSuccEdges( GRAAM graam, DirectedGraphNode graamNode, GRAAMEdgeType graamEdgeType ){
        Set<DirectedGraphNode> graamNodeSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode, graamEdgeType ).spliterator(), false ).collect(Collectors.toSet());
        graamNodeSuccs.forEach( graamNodeSucc -> {
            if( graam.hasEdge( graamNode, graamNodeSucc, graamEdgeType ) )
                graam.removeEdge( graamNode, graamNodeSucc, graamEdgeType );
        } );
        return graamNodeSuccs;
    }

    static Set<Pair<DirectedGraphNode, DirectedGraphNode>> findSwappableNodes(GRAAM graam ){
        Set<Pair<DirectedGraphNode, DirectedGraphNode>> swappableNodes = new HashSet<>();
        Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap = findAllObjectReferences( graam );
        Set<DirectedGraphNode> nonAPINodes = StreamSupport.stream( graam.spliterator(), false ).filter( graamNode -> graamNode instanceof NonFrameworkBoundaryNode ).collect(Collectors.toSet());
        List<DirectedGraphNode> allGraamNodes = StreamSupport.stream(graam.spliterator(), false).collect(Collectors.toList());
        for( int i = 0; i < allGraamNodes.size(); i++ ){
            DirectedGraphNode graamNode1 = allGraamNodes.get(i);
            for( int j = i+1; j < allGraamNodes.size(); j++ ){
                DirectedGraphNode graamNode2 = allGraamNodes.get(j);
                if( graamNode1.equals( graamNode2 ) )
                    continue;
                // If two nodes have the same explicit data parent and explicit data children
                List<DirectedGraphNode> graamNode1Preds = StreamSupport.stream( graam.getPredNodes( graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
                List<DirectedGraphNode> graamNode1Succs = StreamSupport.stream( graam.getSuccNodes( graamNode1, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
                List<DirectedGraphNode> graamNode2Preds = StreamSupport.stream( graam.getPredNodes( graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
                List<DirectedGraphNode> graamNode2Succs = StreamSupport.stream( graam.getSuccNodes( graamNode2, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).collect(Collectors.toList());
                if( !nonAPINodes.contains( graamNode1 ) && !nonAPINodes.contains( graamNode2 ) &&
                        graamNode1Preds.containsAll( graamNode2Preds ) &&
                        graamNode1Succs.containsAll( graamNode2Succs ) &&
                        graamNode2Preds.containsAll( graamNode1Preds ) &&
                        graamNode2Succs.containsAll( graamNode1Succs ))
                    swappableNodes.add( new ImmutablePair<>( graamNode1, graamNode2 ) );

            }
        }

        return swappableNodes;
    }

/*    static boolean isValidEdgeRemoval( GRAAM graam, DirectedGraphNode fromNode, DirectedGraphNode toNode, GRAAMEdgeType graamEdgeType, Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap ){
        if( !toBeConsideredEdgeTypes.contains( graamEdgeType ) ||
                (objectReferenceMap.get(toNode) != null && objectReferenceMap.get(toNode).equals( fromNode ) ))
            return false;

        //TODO: Check whether by removing this edge, we will have two context-based semantically equivalent nodes (which is forbidden!)
        return true;
    }*/

    static Map<DirectedGraphNode, DirectedGraphNode> findAllObjectReferences( GRAAM graam ){
        Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap = new HashMap<>();
        graam.iterator().forEachRemaining( graamNode -> {
            graam.getSuccNodes( graamNode ).forEach( graamNodeSucc -> {
                if( isObjectReference( graam, graamNode, graamNodeSucc ) )
                    objectReferenceMap.put( graamNodeSucc, graamNode );
            } );
        } );
        return objectReferenceMap;
    }

    static boolean isObjectReference( GRAAM graam, DirectedGraphNode potentialReference, DirectedGraphNode graamNode/*, GRAAMEdgeType graamEdgeType*/ ){
        if( !graam.hasEdge( potentialReference, graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ))
            return false;

        if( !(graamNode instanceof FrameworkRelatedNode) || !(potentialReference instanceof FrameworkRelatedNode) )
            return false;
        FrameworkRelatedNode toNodeObjectReferenceNode = GRAAMBuilder.findObjectReferenceNode( graam, (FrameworkRelatedNode) graamNode );
        return potentialReference.equals( toNodeObjectReferenceNode );
    }
}
