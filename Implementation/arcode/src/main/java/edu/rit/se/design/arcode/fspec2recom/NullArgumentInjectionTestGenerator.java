package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class NullArgumentInjectionTestGenerator {
    static List<GRAAMEdgeType> toBeConsideredEdgeTypes = new ArrayList<>();
    static {
        toBeConsideredEdgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP );
//        toBeConsideredEdgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP );

    }

    public static Map<Integer, List<GRAAM>> generateTestCases(GRAAM originalGraam, int maximumNullInjection){
        Map<Integer, List<GRAAM>> generatedTestCases = new HashMap<>();

        List<GRAAM> lastSeed = new ArrayList<>();
        lastSeed.add( GRAAMBuilder.cloneFromScratch( originalGraam ).getKey() );
        int lastNullInjectionNumber = 0;

        while( !lastSeed.isEmpty() && lastNullInjectionNumber < maximumNullInjection ){
            generatedTestCases.put( lastNullInjectionNumber, new ArrayList<>( lastSeed ) );
            List<GRAAM> newSeed = new ArrayList<>();
            lastSeed.forEach( graam -> {
                newSeed.addAll( generateValidRemoveOneEdgeGRAAMs( graam/*, toBeConsideredEdgeTypes*/ ) );
            } );
            lastSeed = findDistinctGRAAMs( newSeed );
            lastNullInjectionNumber++;
        }

        return generatedTestCases;

 /*       generatedTestCases.put(0, )
        if( maximumNullInjection < 1 ) {
            generatedTestCases.add( originalGraam );
            return generatedTestCases;
        }

        List<GRAAM> generationSeeds = generateValidRemoveOneEdgeGRAAMs( originalGraam*//*, toBeConsideredEdgeTypes*//* );
        generationSeeds = findDistinctGRAAMs( generationSeeds );
        generatedTestCases.addAll( generationSeeds );

        int iterationThreshold = maximumNullInjection;
        int iterationCounter = 0;
        while(iterationCounter < iterationThreshold) {
            iterationCounter++;
            List<GRAAM> nextGenerationSeeds = new ArrayList<>();
            generationSeeds.forEach( testGraam -> {
                nextGenerationSeeds.addAll( generateValidRemoveOneEdgeGRAAMs( testGraam*//*, toBeConsideredEdgeTypes*//* ) );
            } );
            generationSeeds = findDistinctGRAAMs( nextGenerationSeeds );
            generationSeeds = nextGenerationSeeds;
            if( generationSeeds.isEmpty() )
                break;
            generatedTestCases.addAll( generationSeeds );
        }
        return generatedTestCases;*/
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

    static List<GRAAM> generateValidRemoveOneEdgeGRAAMs(GRAAM graam ){
        List<GRAAM> allRemoveOneEdgeGRAAMs = new ArrayList<>();
        Map<DirectedGraphNode, Map<DirectedGraphNode, Set<GRAAMEdgeType>>> removableEdges = findRemovableEdges( graam );
        removableEdges.forEach((fromNode, toNodeEdgeTypeMap) -> {
            toNodeEdgeTypeMap.forEach( (toNode, edgeTypes) -> {
                edgeTypes.forEach( graamEdgeType -> {
                    Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGRAAMMap = GRAAMBuilder.cloneFromScratch( graam );
                    GRAAM clonedGRAAM = clonedGRAAMMap.getKey();
                    clonedGRAAM.removeEdge( fromNode, toNode, graamEdgeType );
                    if( clonedGRAAM.getPredNodeCount( toNode ) == 0 )
                        clonedGRAAM.addEdge( clonedGRAAM.getStartNode(), toNode, GRAAMEdgeType.IMPLICIT_DATA_DEP );
                    if( clonedGRAAM.getSuccNodeCount( fromNode ) == 0 )
                        clonedGRAAM.addEdge( fromNode, clonedGRAAM.getEndNode(), GRAAMEdgeType.IMPLICIT_DATA_DEP );


                    allRemoveOneEdgeGRAAMs.add( clonedGRAAM );
                } );
            } );
        });

        return allRemoveOneEdgeGRAAMs;
    }

/*    static List<GRAAM> generateValidRemoveOneEdgeGRAAMs(GRAAM graam, List<GRAAMEdgeType> toBeRemovedEdgeTypes ){
        List<GRAAM> allRemoveOneEdgeGRAAMs = new ArrayList<>();
        graam.iterator().forEachRemaining( graamNode -> {
            for (GRAAMEdgeType graamEdgeType : toBeRemovedEdgeTypes) {
                graam.getSuccNodes( graamNode, graamEdgeType ).forEach( graamNodeSucc -> {
                    if( graam.getPredNodeCount( graamNodeSucc ) > 1 ){
                        Pair<GRAAM, Map<DirectedGraphNode, DirectedGraphNode>> clonedGraamPair = GRAAMBuilder.clone( graam );
                        GRAAM clonedGraam = clonedGraamPair.getKey();
                        Map<DirectedGraphNode, DirectedGraphNode> nodeMappings = clonedGraamPair.getValue();// findNodeMappings( graam, clonedGraam );
                        clonedGraam.removeEdge( nodeMappings.get( graamNode), nodeMappings.get( graamNodeSucc ), graamEdgeType );

                        // Check whether with removing this edge we are producing two nodes (successors of predecessors of nodeMappings.get( graamNodeSucc) ) that
                        // are context-based semantically the same. That would be against the assumptions regarding a GRAAM structure. In that case, we do not
                        // add clonedGraam to allRemoveOneEdgeGRAAMs
                        List<DirectedGraphNode> toBeCheckedNodes = new ArrayList<>();
                        clonedGraam.getPredNodes(nodeMappings.get( graamNodeSucc )).forEach( predNode -> {
                            toBeCheckedNodes.addAll( StreamSupport.stream( clonedGraam.getSuccNodes( predNode ).spliterator(), false ).
                                    collect(Collectors.toList()) );
                        } );

                        for (DirectedGraphNode toBeCheckedNode1 : toBeCheckedNodes) {
                            for (DirectedGraphNode toBeCheckedNode2 : toBeCheckedNodes) {
                                if( !toBeCheckedNode1.equals( toBeCheckedNode2 ) &&
                                        GRAAMBuilder.areContextBaseSemanticallyEqiuvalent( clonedGraam, toBeCheckedNode1,
                                                clonedGraam, toBeCheckedNode2 ) )
                                    return;
                            }
                        }

                        allRemoveOneEdgeGRAAMs.add( clonedGraam );
                    }
                } );
            }
        } );
        return allRemoveOneEdgeGRAAMs;
    }*/

/*    static Map<DirectedGraphNode, DirectedGraphNode> findNodeMappings( GRAAM graam1, GRAAM graam2 ){
        return GRAAMBuilder.findContextBasedNodeMapping( graam1, graam2 );
    }*/

    static Map<DirectedGraphNode, Map<DirectedGraphNode, Set<GRAAMEdgeType>>> findRemovableEdges( GRAAM graam ){
        Map<DirectedGraphNode, Map<DirectedGraphNode, Set<GRAAMEdgeType>>> removableEdges = new HashMap<>();
        Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap = findAllObjectReferences( graam );
        graam.iterator().forEachRemaining( fromNode -> {
            for (GRAAMEdgeType edgeType : toBeConsideredEdgeTypes) {
                graam.getSuccNodes( fromNode, edgeType ).forEach( toNode -> {
                    if( isValidEdgeRemoval( graam, fromNode, toNode, edgeType, objectReferenceMap ) ){
                        if( removableEdges.get( fromNode ) == null )
                            removableEdges.put( fromNode, new HashMap<>());
                        if( removableEdges.get(fromNode).get(toNode) == null )
                            removableEdges.get( fromNode ).put( toNode, new HashSet<>() );
                        removableEdges.get(fromNode).get(toNode).add( edgeType );
                    }
                } );
            }
        } );
        return removableEdges;
    }

    static boolean isValidEdgeRemoval( GRAAM graam, DirectedGraphNode fromNode, DirectedGraphNode toNode, GRAAMEdgeType graamEdgeType, Map<DirectedGraphNode, DirectedGraphNode> objectReferenceMap ){
        if( !toBeConsideredEdgeTypes.contains( graamEdgeType ) ||
                (objectReferenceMap.get(toNode) != null && objectReferenceMap.get(toNode).equals( fromNode ) ))
            return false;

        //TODO: Check whether by removing this edge, we will have two context-based semantically equivalent nodes (which is forbidden!)
        return true;
    }

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
