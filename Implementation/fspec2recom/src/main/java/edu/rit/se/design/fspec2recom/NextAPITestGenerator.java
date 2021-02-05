package edu.rit.se.design.fspec2recom;

import edu.rit.se.design.specminer.graam.*;
import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class NextAPITestGenerator {
    static List<GRAAMEdgeType> toBeConsideredEdgeTypes = new ArrayList<>();
    static {
        toBeConsideredEdgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP );
//        toBeConsideredEdgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP );

    }

    public static Map<Integer, Map<GRAAM, List<GRAAM>>> generateTestCases(GRAAM originalGraam, int maximumRemovedLastAPI){
        Map<Integer, Map<GRAAM, List<GRAAM>>> generatedTestCases = new HashMap<>();

        List<GRAAM> lastSeed = new ArrayList<>();
        lastSeed.add( GRAAMBuilder.cloneFromScratch( originalGraam ).getKey() );
        int lastRemovedLastAPINumber = 0;
        Map<GRAAM, List<GRAAM>> testMap = new HashMap<>();
        testMap.put( lastSeed.get(0), new ArrayList<>(lastSeed) );

        while( !lastSeed.isEmpty() && lastRemovedLastAPINumber < maximumRemovedLastAPI ){
            generatedTestCases.put( lastRemovedLastAPINumber, new HashMap<>( testMap ) );
            testMap.clear();

            List<GRAAM> newSeed = new ArrayList<>();
            lastSeed.forEach( graam -> {
                List<GRAAM> validRemoveLastAPIGRAAMs = generateValidRemoveLastAPIGRAAMs( graam/*, toBeConsideredEdgeTypes*/ );
                validRemoveLastAPIGRAAMs = findDistinctGRAAMs( validRemoveLastAPIGRAAMs );
                testMap.put( graam, validRemoveLastAPIGRAAMs );
                newSeed.addAll( validRemoveLastAPIGRAAMs );
            } );
            lastSeed = findDistinctGRAAMs( newSeed );
            lastRemovedLastAPINumber++;
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

    static List<GRAAM> generateValidRemoveLastAPIGRAAMs(GRAAM graam ){
        List<GRAAM> allRemoveLastNodeGRAAMs = new ArrayList<>();
        Set<DirectedGraphNode> removableNodes = findRemovableNodes( graam );
        removableNodes.forEach( graamNode -> {
            Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGRAAMMap = GRAAMBuilder.cloneFromScratch( graam );
            GRAAM clonedGRAAM = clonedGRAAMMap.getKey();
            removeNodeAndRepairGraam( clonedGRAAM, graamNode );
            allRemoveLastNodeGRAAMs.add( clonedGRAAM );
        } );
        return allRemoveLastNodeGRAAMs;
    }

    // Data dependency relationship is not transitive. So, if we remove a node, we connect its pred. to its succ.
    // with an implicit data dependency edge.
    static void removeNodeAndRepairGraam( GRAAM graam, DirectedGraphNode graamNode ){
        List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNodeSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        graamNodePreds.forEach( graamNodePred -> {
            graamNodeSuccs.forEach( graamNodeSucc -> {
                for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                    if (graam.hasEdge(graamNodePred, graamNode, graamEdgeType)) {
                        if (!graam.hasEdge(graamNodePred, graamNodeSucc))
                            graam.addEdge(graamNodePred, graamNodeSucc, GRAAMEdgeType.IMPLICIT_DATA_DEP);
                        if (graam.hasEdge(graamNode, graamNodeSucc, graamEdgeType)) {
                            if (!graam.hasEdge(graamNodePred, graamNodeSucc))
                                graam.addEdge(graamNodePred, graamNodeSucc, GRAAMEdgeType.IMPLICIT_DATA_DEP);
                        }
                    }
                }
            });
        } );
        graam.removeNodeAndEdges( graamNode );
    }

    static Set<DirectedGraphNode> findRemovableNodes(GRAAM graam ){
        Set<DirectedGraphNode> removableNodes = new HashSet<>();

        int frameworkRelatedNodeCount = (int) StreamSupport.stream( graam.spliterator(), false ).
                filter(directedGraphNode -> directedGraphNode instanceof FrameworkRelatedNode ).count();
        if (frameworkRelatedNodeCount <= 1)
            return removableNodes;

        // If a node has no succ else than end node, then, it is a candidate to be removed
        graam.forEach(directedGraphNode -> {
            if( !(directedGraphNode instanceof FrameworkRelatedNode) )
                return;
            if( StreamSupport.stream(graam.getSuccNodes( directedGraphNode ).spliterator(), false).
                    filter(directedGraphNode1 -> directedGraphNode1 instanceof FrameworkRelatedNode).count() == 0 )
                removableNodes.add( directedGraphNode );

        });
/*
        Set<DirectedGraphNode> objectReferenceNodes = findDefNodes( graam );
        Set<DirectedGraphNode> nonAPINodes = StreamSupport.stream( graam.spliterator(), false ).filter( graamNode -> graamNode instanceof NonFrameworkBoundaryNode ).collect(Collectors.toSet());
        graam.iterator().forEachRemaining( graamNode -> {
            if( !nonAPINodes.contains( graamNode ) && !objectReferenceNodes.contains( graamNode ) )
                removableNodes.add( graamNode );
        } );
*/
        return removableNodes;
    }


/*    static boolean isValidEdgeRemoval( GRAAM graam, DirectedGraphNode fromNode, DirectedGraphNode toNode, GRAAMEdgeType graamEdgeType, Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap ){
        if( !toBeConsideredEdgeTypes.contains( graamEdgeType ) ||
                (objectReferenceMap.get(toNode) != null && objectReferenceMap.get(toNode).equals( fromNode ) ))
            return false;

        //TODO: Check whether by removing this edge, we will have two context-based semantically equivalent nodes (which is forbidden!)
        return true;
    }*/

//    static Map<DirectedGraphNode, DirectedGraphNode> findAllObjectReferences( GRAAM graam ){
//        Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap = new HashMap<>();
//        graam.iterator().forEachRemaining( graamNode -> {
//            graam.getSuccNodes( graamNode ).forEach( graamNodeSucc -> {
//                if( isObjectReference( graam, graamNode, graamNodeSucc ) )
//                    objectReferenceMap.put( graamNodeSucc, graamNode );
//            } );
//        } );
//        return objectReferenceMap;
//    }

    static Set<DirectedGraphNode> findDefNodes(GRAAM graam){
        Set<DirectedGraphNode> objectReferences = new HashSet<>();
        graam.iterator().forEachRemaining( graphNode -> {
            if( !(graphNode instanceof FrameworkRelatedNode) )
                return;
            FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) graphNode;
            Set<FrameworkRelatedNode> defNodes = GRAAMBuilder.findDefNodes( graam, frameworkRelatedNode );
            if( defNodes != null )
                objectReferences.addAll( defNodes );
        } );
        return objectReferences;
    }
//    static boolean isObjectReference( GRAAM graam, DirectedGraphNode potentialReference, DirectedGraphNode graamNode/*, GRAAMEdgeType graamEdgeType*/ ){
//        if( !graam.hasEdge( potentialReference, graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ))
//            return false;
//
//        if( !(graamNode instanceof FrameworkRelatedNode) || !(potentialReference instanceof FrameworkRelatedNode) )
//            return false;
//        FrameworkRelatedNode toNodeObjectReferenceNode = GRAAMBuilder.findObjectReferenceNode( graam, (FrameworkRelatedNode) graamNode );
//        return potentialReference.equals( toNodeObjectReferenceNode );
//    }
}
