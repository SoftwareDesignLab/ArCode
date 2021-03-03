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

package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class GraphEditDistanceComputer {

    static Map<GRAAMEdgeType, FSpecEdgeType> EDGE_TYPE_MAPPING;
    static {
        EDGE_TYPE_MAPPING = new HashMap<>();
        EDGE_TYPE_MAPPING.put( GRAAMEdgeType.EXPLICIT_DATA_DEP, FSpecEdgeType.EXPLICIT_DATA_DEP );
        EDGE_TYPE_MAPPING.put( GRAAMEdgeType.IMPLICIT_DATA_DEP, FSpecEdgeType.IMPLICIT_DATA_DEP );
    }

    public static GraphEditDistanceInfo computeMinEditDistance(GRAAM sourceGraam, SubFSpec subFSpec){
        GraphEditDistanceInfo graphEditDistanceInfo = StreamSupport.stream( computeAllPossibleGraphEditDistances( sourceGraam, subFSpec ).spliterator() , false ).
                min( (o1, o2) -> o1.getDistance() > o2.getDistance() ? 1 : (( o1.getDistance() == o2.getDistance()) ? 0 : -1) ).get();
        return graphEditDistanceInfo;
    }

    static Set<GraphEditDistanceInfo> computeAllPossibleGraphEditDistances(GRAAM sourceGraam, SubFSpec subFSpec ){
        List<Map<DirectedGraphNode, FSpecNode>> allPossibleNodeMappings = findAllPossibleNodeMappings( sourceGraam, subFSpec );
        Set<GraphEditDistanceInfo> allPossibleGraphEdits = new HashSet<>();
        allPossibleNodeMappings.forEach( nodeMapping -> allPossibleGraphEdits.add( computeGraphEditDistance( sourceGraam, subFSpec, nodeMapping) ));
        return allPossibleGraphEdits;
    }

    //TODO: Currently computes only one node context-based mapping while it should find all the possible context-based and non-context-based node mappings
    static List<Map<DirectedGraphNode, FSpecNode>> findAllPossibleNodeMappings(GRAAM sourceGraam, SubFSpec subFSpec ){
        List<Map<DirectedGraphNode, FSpecNode>> nodeMappings = new ArrayList<>();
        Map<DirectedGraphNode, FSpecNode> nodeMapping = /*= new HashMap<>();
        sourceGraam.iterator().forEachRemaining( sourceGraamNode -> {
            Iterator<FSpecNode> distSubFSpecItr = subFSpec.iterator();
            while ( distSubFSpecItr.hasNext() ){
                FSpecNode distFSpecNode = distSubFSpecItr.next();
                if( areConsideredTheSame( sourceGraamNode, distFSpecNode ) ){
                    nodeMapping.put( sourceGraamNode, distFSpecNode );
                }
            }
        } );
        nodeMappings.add( nodeMapping );*/
        FSpecUtil.findContextBasedNodeMapping( subFSpec, sourceGraam, EDGE_TYPE_MAPPING );
        nodeMappings.add( nodeMapping );
        nodeMappings.addAll( createGreedySimpleNodeMappings( sourceGraam, subFSpec) );
        return nodeMappings;
    }

    static List<Map<DirectedGraphNode, FSpecNode>> createGreedySimpleNodeMappings( GRAAM sourceGraam, SubFSpec subFSpec ){
        List<Map<DirectedGraphNode, FSpecNode>> nodeMappings = new ArrayList<>();
        Map<DirectedGraphNode, FSpecNode> nodeMapping = new HashMap<>();
        Set<FSpecNode> mappedFSpecNodes = new HashSet<>();
        sourceGraam.iterator().forEachRemaining( sourceGraamNode -> {
            Iterator<FSpecNode> distSubFSpecItr = subFSpec.iterator();
            while ( distSubFSpecItr.hasNext() ){
                FSpecNode distFSpecNode = distSubFSpecItr.next();
                if( mappedFSpecNodes.contains( distFSpecNode ) )
                    continue;
                if( areConsideredTheSame( sourceGraamNode, distFSpecNode ) ){
                    nodeMapping.put( sourceGraamNode, distFSpecNode );
                    mappedFSpecNodes.add( distFSpecNode );
                }
            }
        } );
        nodeMappings.add( nodeMapping );
        return nodeMappings;
    }

    static boolean areConsideredTheSame(DirectedGraphNode graamNode, FSpecNode fSpecNode){
        return FSpecUtil.areSemanticallyTheSame( fSpecNode, graamNode );
    }

    static GraphEditDistanceInfo computeGraphEditDistance(GRAAM sourceGraam, SubFSpec subFSpec, Map<DirectedGraphNode, FSpecNode> nodeMapping){
        GraphEditDistanceInfo graphEditDistanceInfo = new GraphEditDistanceInfo(sourceGraam, subFSpec);

        List<FSpecNode> toBeAddedNodes = computeToBeAddedNodes( subFSpec, nodeMapping );

        toBeAddedNodes.forEach( toBeAddedNode -> graphEditDistanceInfo.addOperation( new AddNode( toBeAddedNode ) ) );
//        toBeAddedNodes.forEach( toBeAddedNode -> nodeMapping.put( toBeAddedNode, toBeAddedNode ) );

        List<DirectedGraphNode> toBeRemovedNodes = computeToBeRemovedNodes( sourceGraam, nodeMapping );

        Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> toBeRemovedEdges = computeToBeRemovedEdges( sourceGraam, toBeRemovedNodes );

        // I commented below lines to exclude removing edges cost. From now on, we only consider extra APIs that should be removed
        toBeRemovedEdges.keySet().forEach( toBeRmovedEdgeType -> {
            toBeRemovedEdges.get(toBeRmovedEdgeType).forEach( nodePair -> {
                graphEditDistanceInfo.addOperation( new RemoveEdge( toBeRmovedEdgeType, nodePair.getKey(), nodePair.getValue() ) );
            } );
        }  );

        toBeRemovedNodes.forEach( toBeRemovedNode -> graphEditDistanceInfo.addOperation( new RemoveNode( toBeRemovedNode ) ) );

        Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> toBeAddedEdges = computeToBeAddedEdges( sourceGraam, subFSpec, nodeMapping );

        // I commented below lines to exclude adding edges cost. From now on, we only consider APIs that should be added and not their edges.
        toBeAddedEdges.keySet().forEach( toBeAddedEdgeType -> {
            toBeAddedEdges.get(toBeAddedEdgeType).forEach( nodePair -> {
                graphEditDistanceInfo.addOperation( new AddEdge( toBeAddedEdgeType, nodePair.getKey(), nodePair.getValue() ) );
            } );
        }  );

        return graphEditDistanceInfo;
    }

    static List<FSpecNode> computeToBeAddedNodes( SubFSpec distGraam, Map<DirectedGraphNode, FSpecNode> nodeMapping ){
        List<FSpecNode> toBeAddedNodes = StreamSupport.stream( distGraam.spliterator(), false ).collect(Collectors.toList());
        toBeAddedNodes.removeAll( nodeMapping.values() );
        return toBeAddedNodes;
    }

    static List<DirectedGraphNode> computeToBeRemovedNodes( GRAAM sourceGraam, Map<DirectedGraphNode, FSpecNode> nodeMapping ){
        List<DirectedGraphNode> toBeRemovedNodes = StreamSupport.stream( sourceGraam.spliterator(), false ).collect(Collectors.toList());
        toBeRemovedNodes.removeAll( nodeMapping.keySet() );
        return toBeRemovedNodes;
    }

    // FIXME: Here we compute edges to be removed based on nodes we found needed to be removed. However, based on the change we made to
    // findNodeMappings, we are now considering non-context-based node mappings as well. So, in that case, we can not rely on
    // the toBeRemovedNodeList since in such a case all nodes might be (non-context-based) mapped while edges are not mapped and needed
    // to be removed/added
    static Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> computeToBeRemovedEdges( GRAAM sourceGraam, List<DirectedGraphNode> toBeRemovedNodes ){
        Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> toBeRemovedEdges = new HashMap<>();
        toBeRemovedNodes.iterator().forEachRemaining( toBeRemovedNode -> {

            for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                if( !toBeRemovedEdges.containsKey( graamEdgeType ) )
                    toBeRemovedEdges.put( graamEdgeType, new ArrayList<>() );

                sourceGraam.getSuccNodes( toBeRemovedNode, graamEdgeType ).forEach( toBeRemovedNodeSucc -> {
                    toBeRemovedEdges.get( graamEdgeType ).add( new ImmutablePair<>( toBeRemovedNode, toBeRemovedNodeSucc ) );
                } );

                sourceGraam.getPredNodes( toBeRemovedNode, graamEdgeType ).forEach( toBeRemovedNodePred -> {
                    toBeRemovedEdges.get( graamEdgeType ).add( new ImmutablePair<>( toBeRemovedNodePred, toBeRemovedNode ) );
                } );
            }
        } );

        for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
            if( toBeRemovedEdges.get( graamEdgeType ) != null )
                toBeRemovedEdges.put( graamEdgeType, toBeRemovedEdges.get( graamEdgeType ).stream().distinct().collect(Collectors.toList()));
        }

        return toBeRemovedEdges;
    }


    static Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> computeToBeAddedEdges( GRAAM sourceGraam, SubFSpec subFSpec, Map<DirectedGraphNode, FSpecNode> nodeMapping){
        Map<GRAAMEdgeType, List<Pair<DirectedGraphNode, DirectedGraphNode>>> toBeAddedEdges = new HashMap<>();

        Map<FSpecNode, DirectedGraphNode> reversedNodeMapping = reverseNodeMapping( nodeMapping );

        subFSpec.iterator().forEachRemaining( distNode -> {

            for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
                FSpecEdgeType fSpecEdgeType = EDGE_TYPE_MAPPING.get( graamEdgeType );
                if( !toBeAddedEdges.containsKey( graamEdgeType ) )
                    toBeAddedEdges.put( graamEdgeType, new ArrayList<>() );

                subFSpec.getSuccNodes( distNode, fSpecEdgeType ).forEach( distNodeSucc -> {
                    // If either of corresponding graamnodes to fspecnodes are not in the graam (theu are new nodes),
                    // replace them with the corresponding fspecNode in the AddEdge operation to avoid null references.

                    DirectedGraphNode srcNode = reversedNodeMapping.get( distNode ) != null ? reversedNodeMapping.get( distNode ) : distNode;
                    DirectedGraphNode srcNodeSucc = reversedNodeMapping.get( distNodeSucc ) != null ? reversedNodeMapping.get( distNodeSucc ) : distNodeSucc;

                    if( subFSpec.hasEdge( distNode, distNodeSucc, fSpecEdgeType ) &&
                            (!sourceGraam.containsNode( srcNode ) || !sourceGraam.containsNode(srcNodeSucc) || !sourceGraam.hasEdge( srcNode, srcNodeSucc, graamEdgeType )
                            )
                    )
                        toBeAddedEdges.get( graamEdgeType ).add( new ImmutablePair<>( srcNode, srcNodeSucc ) );
                } );

                subFSpec.getPredNodes( distNode, fSpecEdgeType ).forEach( distNodePred -> {
                    DirectedGraphNode srcNode = reversedNodeMapping.get( distNode ) != null ? reversedNodeMapping.get( distNode ) : distNode;
                    DirectedGraphNode srcNodePred = reversedNodeMapping.get( distNodePred ) != null ? reversedNodeMapping.get( distNodePred ) : distNodePred;
                    if( subFSpec.hasEdge( distNodePred, distNode, fSpecEdgeType ) &&
                            (!sourceGraam.containsNode( srcNode ) || !sourceGraam.containsNode(srcNodePred) || !sourceGraam.hasEdge( srcNodePred, srcNode, graamEdgeType )
                            )
                    )
                        toBeAddedEdges.get( graamEdgeType ).add( new ImmutablePair<>( srcNodePred, srcNode ) );


                } );
            }
        } );

        for (GRAAMEdgeType graamEdgeType : GRAAMEdgeType.values()) {
            toBeAddedEdges.put( graamEdgeType, toBeAddedEdges.get( graamEdgeType ).stream().distinct().collect(Collectors.toList()));
        }

        return toBeAddedEdges;
    }

    static Map<FSpecNode, DirectedGraphNode> reverseNodeMapping( Map<DirectedGraphNode, FSpecNode> nodeMapping ){
        Map<FSpecNode, DirectedGraphNode> reversedNodeMapping = new HashMap<>();
        nodeMapping.forEach( (from, to) -> reversedNodeMapping.put( to, from ) );
        return reversedNodeMapping;
    }
}
