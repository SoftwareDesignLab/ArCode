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
public class MissedAPITestGenerator {
    static List<GRAAMEdgeType> toBeConsideredEdgeTypes = new ArrayList<>();
    static {
        toBeConsideredEdgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP );
//        toBeConsideredEdgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP );

    }

    public static Map<Integer, List<GRAAM>> generateTestCases(GRAAM originalGraam, int maximumMissedAPI){
        Map<Integer, List<GRAAM>> generatedTestCases = new HashMap<>();

        List<GRAAM> lastSeed = new ArrayList<>();
        lastSeed.add( GRAAMBuilder.cloneFromScratch( originalGraam ).getKey() );
        int lastMissedAPINumber = 0;

        while( !lastSeed.isEmpty() && lastMissedAPINumber < maximumMissedAPI ){
            generatedTestCases.put( lastMissedAPINumber, new ArrayList<>( lastSeed ) );
            List<GRAAM> newSeed = new ArrayList<>();
            lastSeed.forEach( graam -> {
                newSeed.addAll( generateValidRemoveOneAPIGRAAMs( graam/*, toBeConsideredEdgeTypes*/ ) );
            } );
            lastSeed = findDistinctGRAAMs( newSeed );
            lastMissedAPINumber++;
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

    static List<GRAAM> generateValidRemoveOneAPIGRAAMs(GRAAM graam ){
        List<GRAAM> allRemoveOneNodeGRAAMs = new ArrayList<>();
        Set<DirectedGraphNode> removableNodes = findRemovableNodes( graam );
        removableNodes.forEach( graamNode -> {
            Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGRAAMMap = GRAAMBuilder.cloneFromScratch( graam );
            GRAAM clonedGRAAM = clonedGRAAMMap.getKey();
            removeNodeAndRepairGraam( clonedGRAAM, graamNode );
            allRemoveOneNodeGRAAMs.add( clonedGRAAM );
        } );
        return allRemoveOneNodeGRAAMs;
    }

    static void removeNodeAndRepairGraam( GRAAM graam, DirectedGraphNode graamNode ){
        List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNodeSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        graamNodePreds.forEach( graamNodePred -> {
            graamNodeSuccs.forEach( graamNodeSucc -> {
                for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                    if (graam.hasEdge(graamNodePred, graamNode, graamEdgeType)) {
                        if (!graam.hasEdge(graamNodePred, graamNodeSucc, graamEdgeType))
                            graam.addEdge(graamNodePred, graamNodeSucc, graamEdgeType);
                        if (graam.hasEdge(graamNode, graamNodeSucc, graamEdgeType)) {
                            if (!graam.hasEdge(graamNodePred, graamNodeSucc, graamEdgeType))
                                graam.addEdge(graamNodePred, graamNodeSucc, graamEdgeType);
                        }
                    }
                }
            });
        } );
        graam.removeNodeAndEdges( graamNode );
    }

    static Set<DirectedGraphNode> findRemovableNodes(GRAAM graam ){
        Set<DirectedGraphNode> removableNodes = new HashSet<>();
        Set<DirectedGraphNode> objectReferenceNodes = findAllObjectReferences( graam );
        Set<DirectedGraphNode> nonAPINodes = StreamSupport.stream( graam.spliterator(), false ).filter( graamNode -> graamNode instanceof NonFrameworkBoundaryNode ).collect(Collectors.toSet());
        graam.iterator().forEachRemaining( graamNode -> {
            if( !nonAPINodes.contains( graamNode ) && !objectReferenceNodes.contains( graamNode ) )
                removableNodes.add( graamNode );
        } );
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

    static Set<DirectedGraphNode> findAllObjectReferences(GRAAM graam){
        Set<DirectedGraphNode> objectReferences = new HashSet<>();
        graam.iterator().forEachRemaining( graphNode -> {
            if( !(graphNode instanceof FrameworkRelatedNode) )
                return;
            FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) graphNode;
            FrameworkRelatedNode objectReference = GRAAMBuilder.findObjectReferenceNode( graam, frameworkRelatedNode );
            if( objectReference != null )
                objectReferences.add( objectReference );
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
