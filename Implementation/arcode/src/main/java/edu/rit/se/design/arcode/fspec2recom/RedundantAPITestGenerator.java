package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class RedundantAPITestGenerator {
    static List<GRAAMEdgeType> toBeConsideredEdgeTypes = new ArrayList<>();
    static {
        toBeConsideredEdgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP );
//        toBeConsideredEdgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP );

    }

    public static Map<Integer, List<GRAAM>> generateTestCases(GRAAM originalGraam, int maximumRedundantAPI){
        Map<Integer, List<GRAAM>> generatedTestCases = new HashMap<>();

        List<GRAAM> lastSeed = new ArrayList<>();
        lastSeed.add( GRAAMBuilder.cloneFromScratch( originalGraam ).getKey() );
        int lastRedundantAPINumber = 0;

        while( !lastSeed.isEmpty() && lastRedundantAPINumber < maximumRedundantAPI ){
            generatedTestCases.put( lastRedundantAPINumber, new ArrayList<>( lastSeed ) );
            List<GRAAM> newSeed = new ArrayList<>();
            lastSeed.forEach( graam -> {
                newSeed.addAll( generateValidRedundantOneAPIGRAAMs( graam/*, toBeConsideredEdgeTypes*/ ) );
            } );
            lastSeed = findDistinctGRAAMs( newSeed );
            lastRedundantAPINumber++;
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

    static List<GRAAM> generateValidRedundantOneAPIGRAAMs(GRAAM graam ){
        List<GRAAM> allRedundantOneNodeGRAAMs = new ArrayList<>();
        Set<DirectedGraphNode> redundantableNodes = findRedundantableNodes( graam );
        redundantableNodes.forEach( graamNode -> {
            Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGRAAMMap = GRAAMBuilder.cloneFromScratch( graam );
            GRAAM clonedGRAAM = clonedGRAAMMap.getKey();
            redundantNodeAndRepairGraam( clonedGRAAM, graamNode );
            allRedundantOneNodeGRAAMs.add( clonedGRAAM );
        } );
        return allRedundantOneNodeGRAAMs;
    }

    static void redundantNodeAndRepairGraam(GRAAM graam, DirectedGraphNode graamNode ){
        List<DirectedGraphNode> graamNodePreds = StreamSupport.stream( graam.getPredNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        List<DirectedGraphNode> graamNodeSuccs = StreamSupport.stream( graam.getSuccNodes( graamNode ).spliterator(), false ).collect(Collectors.toList());
        DirectedGraphNode clonedGraamNode = GRAAMBuilder.cloneNodeFromScratch( graamNode );
        graam.addNode( clonedGraamNode );
        graamNodePreds.forEach( graamNodePred -> {
            for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                if (graam.hasEdge(graamNodePred, graamNode, graamEdgeType) && !graam.hasEdge( graamNodePred, clonedGraamNode, graamEdgeType )) {
                    graam.addEdge(graamNodePred, clonedGraamNode, graamEdgeType);
                }
            }
        } );
        graamNodeSuccs.forEach( graamNodeSucc -> {
            for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                if (graam.hasEdge(graamNode, graamNodeSucc, graamEdgeType) && !graam.hasEdge( clonedGraamNode, graamNodeSucc, graamEdgeType )) {
                    graam.addEdge(clonedGraamNode, graamNodeSucc, graamEdgeType);
                }
            }
        } );
    }

    static Set<DirectedGraphNode> findRedundantableNodes(GRAAM graam ){
        Set<DirectedGraphNode> redundantableNodes = new HashSet<>();
//        Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap = findAllObjectReferences( graam );
        Set<DirectedGraphNode> nonAPINodes = StreamSupport.stream( graam.spliterator(), false ).filter( graamNode -> graamNode instanceof NonFrameworkBoundaryNode ).collect(Collectors.toSet());
        graam.iterator().forEachRemaining( graamNode -> {
            if( !nonAPINodes.contains( graamNode ) /*&& !objectReferenceMap.containsKey( graamNode ) */)
                redundantableNodes.add( graamNode );
        } );
        return redundantableNodes;
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
