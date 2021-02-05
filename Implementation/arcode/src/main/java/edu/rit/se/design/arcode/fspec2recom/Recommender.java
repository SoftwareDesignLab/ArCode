package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class Recommender {

    static Map<GRAAMEdgeType, FSpecEdgeType> EDGE_TYPE_MAPPING;
    static{
        EDGE_TYPE_MAPPING = new HashMap<>();
        EDGE_TYPE_MAPPING.put( GRAAMEdgeType.EXPLICIT_DATA_DEP, FSpecEdgeType.EXPLICIT_DATA_DEP  );
        EDGE_TYPE_MAPPING.put( GRAAMEdgeType.IMPLICIT_DATA_DEP, FSpecEdgeType.IMPLICIT_DATA_DEP );
    }

    public static List<GraphEditDistanceInfo> findRankedRecommendations(GRAAM sourceGraam, FSpec fSpec ){
        List<GraphEditDistanceInfo> rankedRecommendations = new ArrayList<>();
        List<GraphEditDistanceInfo> finalRankedRecommendations = rankedRecommendations;
        List<SubFSpec> allUsecases = createAllSubFSpecs( fSpec );
        allUsecases.forEach(subFSpec -> finalRankedRecommendations.add( GraphEditDistanceComputer.computeMinEditDistance( sourceGraam, subFSpec ) ) );
        rankedRecommendations = rankedRecommendations.stream().sorted( (graphEditDistanceInfo1, graphEditDistanceInfo2) -> graphEditDistanceInfo1.getDistance() > graphEditDistanceInfo2.getDistance() ? 1 : (graphEditDistanceInfo1.getDistance() == graphEditDistanceInfo2.getDistance() ? 0 : -1) ).collect(Collectors.toList());
        rankedRecommendations = rankedRecommendations.stream()/*.filter( graphEditDistanceInfo -> graphEditDistanceInfo.getDistance() <= 3 )*/.collect(Collectors.toList());
        return rankedRecommendations;
    }

    static List<SubFSpec> createAllSubFSpecs( FSpec fSpec ){
        List<SubFSpec> allFSpecs = new ArrayList<>();
        List<FSpecNode> endNodes = StreamSupport.stream( fSpec.spliterator(), false ).filter( fSpecNode -> fSpecNode instanceof FSpecEndNode).collect(Collectors.toList());
        endNodes.forEach( fSpecNode -> {
            SubFSpec subFSpec = new SubFSpec( fSpec );
            subFSpec.addNode( fSpecNode );
            createSubFSpec( fSpec, (FSpecEndNode)fSpecNode, subFSpec );
            allFSpecs.add( subFSpec );
        } );
        return allFSpecs;
    }

    static void createSubFSpec( FSpec fSpec, FSpecNode fSpecNode, SubFSpec subFSpec ){
        if( fSpecNode instanceof FSpecStartNode )
            return;
        for (FSpecEdgeType fSpecEdgeType : FSpecEdgeType.values()) {
            List<FSpecNode> fSpecNodePreds = StreamSupport.stream( fSpec.getPredNodes( fSpecNode, fSpecEdgeType ).spliterator(), false ).collect(Collectors.toList());
            fSpecNodePreds.forEach( fSpecNodePred -> {
                if( !subFSpec.containsNode( fSpecNodePred ) )
                    subFSpec.addNode( fSpecNodePred );
                if( !subFSpec.hasEdge( fSpecNodePred, fSpecNode, fSpecEdgeType ) )
                    subFSpec.addEdge( fSpecNodePred, fSpecNode, fSpecEdgeType );
                createSubFSpec( fSpec, fSpecNodePred, subFSpec );
            } );
        }
    }

    public static SubFSpec applyGraphEdit( GRAAM graam, FSpec fSpec, GraphEditDistanceInfo graphEditDistanceInfo ){
        SubFSpec subFSpec = graam2SubFSpec( graam, fSpec );
        Map<DirectedGraphNode, FSpecNode> nodeMapping = FSpecUtil.findContextBasedNodeMapping( subFSpec, graam, EDGE_TYPE_MAPPING );
        applyGraphEdit( subFSpec, graphEditDistanceInfo, nodeMapping);
        return subFSpec;
    }

    static void applyGraphEdit( SubFSpec subFSpec, GraphEditDistanceInfo graphEditDistanceInfo, Map<DirectedGraphNode, FSpecNode> nodeMapping ){
        graphEditDistanceInfo.operations.forEach( graphEditOperation -> applyGraphEditOperation( subFSpec, graphEditOperation, nodeMapping ) );
    }

    static void applyGraphEditOperation( SubFSpec subFSpec, GraphEditOperation graphEditOperation, Map<DirectedGraphNode, FSpecNode> nodeMapping ){
        if( graphEditOperation instanceof AddNode )
            if(!subFSpec.containsNode( (FSpecNode) ((AddNode) graphEditOperation).addedNode ) )
                subFSpec.addNode((FSpecNode) ((AddNode) graphEditOperation).addedNode);
        if( graphEditOperation instanceof RemoveNode )
            subFSpec.removeNode( nodeMapping.get ( ((RemoveNode) graphEditOperation).removedNode ) );
        if( graphEditOperation instanceof RemoveEdge ){
            FSpecNode fSpecFromNode = nodeMapping.get( ((RemoveEdge)graphEditOperation).fromNode );
            FSpecNode fSpecToNode = nodeMapping.get( ((RemoveEdge)graphEditOperation).toNode );
            subFSpec.removeEdge( fSpecFromNode, fSpecToNode, EDGE_TYPE_MAPPING.get (((RemoveEdge)graphEditOperation).edgeType) );
        }
        if( graphEditOperation instanceof AddEdge ){
            FSpecNode fSpecFromNode = ((AddEdge)graphEditOperation).fromNode instanceof FSpecNode ?
                    (FSpecNode) ((AddEdge)graphEditOperation).fromNode : nodeMapping.get( ((AddEdge)graphEditOperation).fromNode );
            FSpecNode fSpecToNode = ((AddEdge)graphEditOperation).toNode instanceof FSpecNode ?
                    (FSpecNode) ((AddEdge)graphEditOperation).toNode : nodeMapping.get( ((AddEdge)graphEditOperation).toNode );
            subFSpec.addEdge( fSpecFromNode, fSpecToNode, EDGE_TYPE_MAPPING.get (((AddEdge)graphEditOperation).edgeType) );
        }

    }

    static SubFSpec graam2SubFSpec( GRAAM graam, FSpec fSpec ){
        SubFSpec subFSpec = fSpec2SubFSpec( fSpec );
        FSpecBuilder.mergeGRAAMWithFSpec( subFSpec, graam, EDGE_TYPE_MAPPING );
        Map<DirectedGraphNode, FSpecNode> nodeMapping = FSpecUtil.findContextBasedNodeMapping( subFSpec, graam, EDGE_TYPE_MAPPING );

        Map<FSpecNode, DirectedGraphNode> reversedNodeMapping = new HashMap<>();
        nodeMapping.keySet().forEach( directedGraphNode -> reversedNodeMapping.put( nodeMapping.get( directedGraphNode ), directedGraphNode ) );

        List<FSpecNode> toBeRemovedNodes = StreamSupport.stream(subFSpec.spliterator(), false).filter(fSpecNode -> !nodeMapping.values().contains(fSpecNode) ).collect(Collectors.toList());
        toBeRemovedNodes.forEach( fSpecNode -> subFSpec.removeNodeAndEdges( fSpecNode ) );

        subFSpec.iterator().forEachRemaining( fSpecNode -> {
            for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                FSpecEdgeType fSpecEdgeType = EDGE_TYPE_MAPPING.get( graamEdgeType );
                subFSpec.getSuccNodes( fSpecNode, fSpecEdgeType ).forEach( fSpecNodeSucc -> {
                    DirectedGraphNode graamNode = reversedNodeMapping.get( fSpecNode );
                    DirectedGraphNode graamNodeSucc = reversedNodeMapping.get( fSpecNodeSucc );
                    if( !graam.hasEdge( graamNode, graamNodeSucc, graamEdgeType ) )
                        subFSpec.removeEdge( fSpecNode, fSpecNodeSucc, fSpecEdgeType );
                } );

                subFSpec.getPredNodes( fSpecNode, fSpecEdgeType ).forEach( fSpecNodePred -> {
                    DirectedGraphNode graamNode = reversedNodeMapping.get( fSpecNode );
                    DirectedGraphNode graamNodePred = reversedNodeMapping.get( fSpecNodePred );
                    if( !graam.hasEdge( graamNodePred, graamNode , graamEdgeType ) )
                        subFSpec.removeEdge(  fSpecNodePred, fSpecNode, fSpecEdgeType );
                } );

            }
        } );

        return subFSpec;
    }

    static SubFSpec fSpec2SubFSpec( FSpec fSpec ){
        SubFSpec subFSpec = new SubFSpec( fSpec );
        fSpec.iterator().forEachRemaining( fSpecNode -> {if( !subFSpec.containsNode( fSpecNode ) ) subFSpec.addNode( fSpecNode );} );

        fSpec.iterator().forEachRemaining( fSpecNode -> {
            for (FSpecEdgeType fSpecEdgeType : FSpecEdgeType.values()) {
                fSpec.getSuccNodes( fSpecNode, fSpecEdgeType ).forEach( fSpecNodeSucc -> {
                    if( !subFSpec.hasEdge( fSpecNode, fSpecNodeSucc, fSpecEdgeType ) )
                        subFSpec.addEdge( fSpecNode, fSpecNodeSucc, fSpecEdgeType );
                } );

                fSpec.getPredNodes( fSpecNode, fSpecEdgeType ).forEach( fSpecNodePred -> {
                    if( !subFSpec.hasEdge( fSpecNodePred, fSpecNode, fSpecEdgeType ) )
                        subFSpec.addEdge( fSpecNodePred, fSpecNode, fSpecEdgeType );
                } );
            }
        } );

        return subFSpec;
    }

    public static boolean areTheSame( GRAAM graam, SubFSpec subFSpec ){
        Map<DirectedGraphNode, FSpecNode> nodeMapping = FSpecUtil.findContextBasedNodeMapping( subFSpec, graam, EDGE_TYPE_MAPPING );
        return nodeMapping.keySet().containsAll( graam.getNodeSet() ) && graam.getNodeSet().containsAll( nodeMapping.keySet() ) &&
                nodeMapping.values().containsAll( subFSpec.getNodeSet() ) && subFSpec.getNodeSet().containsAll(  nodeMapping.values()) ;
    }

    public static int getMatchedRecommendationRank( GRAAM expectedGraam, List<GraphEditDistanceInfo> rankedRecommendations ){
        for( int i = 0; i < rankedRecommendations.size(); i++ )
            if( areTheSame( expectedGraam, rankedRecommendations.get(i).distSubFSpec ) )
                return i + 1;
        return 0;
    }

    public static boolean embedsIn( GRAAM graam, SubFSpec subFSpec ){
        Map<DirectedGraphNode, FSpecNode> nodeMapping = FSpecUtil.findContextBasedNodeMapping( subFSpec, graam, EDGE_TYPE_MAPPING );

        Set<DirectedGraphNode> toBeConsideredGRAAMNodes = new HashSet<>( graam.getNodeSet() );
        toBeConsideredGRAAMNodes.remove( graam.getEndNode() );


        return nodeMapping.keySet().containsAll( toBeConsideredGRAAMNodes ) && graam.getNodeSet().containsAll( nodeMapping.keySet() ) /*&&
                nodeMapping.values().containsAll( subFSpec.getNodeSet() ) && subFSpec.getNodeSet().containsAll(  nodeMapping.values())*/ ;
    }

    public static int getEmbededRecommendationRank( GRAAM expectedGraam, List<GraphEditDistanceInfo> rankedRecommendations ){
        Set<Integer> distinctDistances = new HashSet<>();
        rankedRecommendations.forEach( graphEditDistanceInfo -> distinctDistances.add( graphEditDistanceInfo.getDistance() ) );
        List<Integer> sortedDistances = new ArrayList<Integer>(distinctDistances);
        Map<Integer, Integer> distancesRank = new HashMap<>();
        for( int i = 0; i < rankedRecommendations.size(); i++ ){
            GraphEditDistanceInfo graphEditDistanceInfo = rankedRecommendations.get(i);
            if ( !distancesRank.containsKey( graphEditDistanceInfo.getDistance() ) )
                distancesRank.put( graphEditDistanceInfo.getDistance(), i + 1 );
        }
        Collections.sort( sortedDistances );
        for( int i = 0; i < rankedRecommendations.size(); i++ )
            if( embedsIn( expectedGraam, rankedRecommendations.get(i).distSubFSpec ) )
                return /*i + 1*/distancesRank.get( rankedRecommendations.get(i).getDistance() );
        return 0;
    }



}
