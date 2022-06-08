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

package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;
/*import edu.rit.se.design.graphs.ds.EdgeException;
import edu.rit.se.design.graphs.ds.NodeException;
import edu.rit.se.design.graphs.graam.*;*/
import edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset.FrequentSequencePattern;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.ApiRepresentation;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.LevenshteinSimilarityUtil;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MapoApiCallSeq;
import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MapoMissedApiEvaluation implements IModelEvaluation<MapoMissedApiEvaluationResult> {
    List<GRAAM> testGRAAMList;
    Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns;

    public MapoMissedApiEvaluation(Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns,
                                   List<GRAAM> testGRAAMList){
        setTestGRAAMList( testGRAAMList );
        setFoundPatterns(foundPatterns);
    }

    public Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> getFoundPatterns() {
        return foundPatterns;
    }

    public void setFoundPatterns(Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns) {
        this.foundPatterns = foundPatterns;
    }

    public MapoMissedApiEvaluationResult evaluate(){
        MapoMissedApiEvaluationResult mapoMissedApiEvaluationResult = new MapoMissedApiEvaluationResult();
        //TODO: count number of patterns nodes and edges
        mapoMissedApiEvaluationResult.setTotalModelNodes( 0 );
        mapoMissedApiEvaluationResult.setTotalModelEdges( 0 );
        for( GRAAM graam: getTestGRAAMList() ) {
            mapoMissedApiEvaluationResult.increaseTotalTestGRAAMs();
            GRAAM clonedGRAAM = GRAAMBuilder.cloneFromScratch(graam).getLeft();

            // Listing all the non-root and non-leaf nodes of the clonedGRAAM. We need this list to iteratively replace one node
            // with a GRAAMHoleNode and then, use the fsm to find the corresponding DirectedGraphNode.
            // The reason why we remove leaf nodes is because we use leaf nodes in NextAPI evaluation.
            List<DirectedGraphNode> toBeRecommendedAPIs = new ArrayList( clonedGRAAM.getNodeSet() );
            for (DirectedGraphNode clonedDirectedGraphNode: clonedGRAAM.getNodeSet() )
                if( clonedDirectedGraphNode instanceof NonFrameworkRelatedNode /*|| clonedGRAAM.getLeafNodes(GRAAMEdge.GRAAMEdgeType.DOMINANCE).contains(clonedDirectedGraphNode)*/ ) {
                    toBeRecommendedAPIs.remove(clonedDirectedGraphNode);
                }

            while (toBeRecommendedAPIs.size() > 0) {
                mapoMissedApiEvaluationResult.increaseTotalExperiment();
                DirectedGraphNode toBeRecommendedAPI = toBeRecommendedAPIs.remove(0);

                int solutionRank = -1;

                try {

                    Iterable<DirectedGraphNode> toBeRecommendedAPIParents = clonedGRAAM.getPredNodes( toBeRecommendedAPI, GRAAMEdgeType.EXPLICIT_DATA_DEP );
                    Iterable<DirectedGraphNode> toBeRecommendedAPIChildren = clonedGRAAM.getSuccNodes( toBeRecommendedAPI, GRAAMEdgeType.EXPLICIT_DATA_DEP );
                    List<FrequentSequencePattern<MapoApiCallSeq>> foundCandidatePatterns = findCandidatePatterns(
                            toBeRecommendedAPIParents, toBeRecommendedAPIChildren );
                    List<DirectedGraphNode> rankedSolutions = getRankedSolutions( foundCandidatePatterns, toBeRecommendedAPIParents, toBeRecommendedAPIChildren );


                    for( int rank= 0; rank < rankedSolutions.size(); rank++ ) {
                        DirectedGraphNode recommendedAPI = rankedSolutions.get(rank);
                        if ( areSemanticallyTheSame(recommendedAPI, toBeRecommendedAPI)) {
                            solutionRank = rank + 1;
                            break;
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }

                if(solutionRank > 0)
                    mapoMissedApiEvaluationResult.increaseTotalHit( solutionRank );
                else
                    mapoMissedApiEvaluationResult.increaseTotalMissed();
            }
        }


        return mapoMissedApiEvaluationResult;
    }

    boolean areSemanticallyTheSame(DirectedGraphNode recommendedAPI, DirectedGraphNode graamNode){
        return recommendedAPI != null && GRAAMBuilder.areSemanticallyTheSame(recommendedAPI,graamNode);
    }

    List<DirectedGraphNode> getRankedSolutions( Iterable<FrequentSequencePattern<MapoApiCallSeq>> mapoCandidatePatterns,
                                        Iterable<DirectedGraphNode> toBeRecommendedAPIParents, Iterable<DirectedGraphNode> toBeRecommendedAPIChildren ) {
        List<DirectedGraphNode> rankedRecommendations = new ArrayList<>();
        Map<Double, List<FrequentSequencePattern<MapoApiCallSeq>>> candidatePatternsScore = new HashMap<>();

        //Computing patterns scores
        mapoCandidatePatterns.forEach( mapoApiCallSeqFrequentSequencePattern -> {
                    double patternScore = computePatternScore(mapoApiCallSeqFrequentSequencePattern, toBeRecommendedAPIParents);
                    if (candidatePatternsScore.get(patternScore) == null)
                        candidatePatternsScore.put(patternScore, new ArrayList<>());
                    candidatePatternsScore.get(patternScore).add(mapoApiCallSeqFrequentSequencePattern);
                });
        List<Double> sortedScores = new ArrayList<>( candidatePatternsScore.keySet() );
        sortedScores.sort( (o1, o2) -> (o1 < o2 ? 1 : (o1 == o2 ? 0 : -1)) );
        sortedScores.forEach( score -> {
            candidatePatternsScore.get( score ).forEach( mapoApiCallSeqFrequentSequencePattern -> {
                mapoApiCallSeqFrequentSequencePattern.getCorrespondingGraamSet().forEach(graam -> {
                    rankedRecommendations.addAll(findCorrespondingDirectedGraphNode(toBeRecommendedAPIParents, toBeRecommendedAPIChildren, graam));
                });
            });
        } );

        return rankedRecommendations;
    }

    List<DirectedGraphNode> findCorrespondingDirectedGraphNode(Iterable<DirectedGraphNode> correspondingDirectedGraphNodeParents, Iterable<DirectedGraphNode> correspondingDirectedGraphNodeChildren,
                                               GRAAM graam){
        List<DirectedGraphNode> foundCorrespondingDirectedGraphNodes = new ArrayList<>();
        graam.getNodeSet().forEach( graamNode -> {
            boolean hasAllCorrespondingParents = true;
            try {
                List<DirectedGraphNode> graamNodeParents = StreamSupport.stream(graam.getPredNodes( graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false).collect(Collectors.toList());
                List<DirectedGraphNode> clonedExpectedParents = StreamSupport.stream(correspondingDirectedGraphNodeParents.spliterator(), false).collect(Collectors.toList());

                List<DirectedGraphNode> graamNodeChildren = StreamSupport.stream(graam.getSuccNodes( graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false).collect(Collectors.toList());
                List<DirectedGraphNode> clonedExpectedChildren = StreamSupport.stream(correspondingDirectedGraphNodeChildren.spliterator(), false).collect(Collectors.toList());


                if( (graamNodeParents.size() != clonedExpectedParents.size()) ||
                        (graamNodeChildren.size() != clonedExpectedChildren.size())
                )
                    return;

                graamNodeParents.forEach( graamNodeParent -> {

                        for (DirectedGraphNode expectedParent: clonedExpectedParents){
                            if( GRAAMBuilder.areSemanticallyTheSame(expectedParent, graamNodeParent ) ){
                                clonedExpectedParents.remove(expectedParent);
                                break;
                            }
                        }
                    } );

                graamNodeChildren.forEach( graamNodeChild -> {

                    for (DirectedGraphNode expectedChild: clonedExpectedChildren){
                        if( GRAAMBuilder.areSemanticallyTheSame(expectedChild,graamNodeChild ) ){
                            clonedExpectedChildren.remove(expectedChild);
                            break;
                        }
                    }
                } );

                if( clonedExpectedChildren.size() == 0 && clonedExpectedParents.size() == 0 )
                    foundCorrespondingDirectedGraphNodes.add( graamNode );


            } catch (Exception e) {
                e.printStackTrace();
            }
            if(hasAllCorrespondingParents)
                foundCorrespondingDirectedGraphNodes.add( graamNode );
        } );
        return foundCorrespondingDirectedGraphNodes;
    }

    List<FrequentSequencePattern<MapoApiCallSeq>> findCandidatePatterns(Iterable<DirectedGraphNode> toBeRecommendedAPIParents, Iterable<DirectedGraphNode> toBeRecommendedAPIChildren){
        List<FrequentSequencePattern<MapoApiCallSeq>> candidatePatterns = new ArrayList<>();
        foundPatterns.forEach( (s, frequentSequencePatterns) ->  {
            frequentSequencePatterns.forEach( mapoApiFrequentSequencePattern -> {
                for( DirectedGraphNode toBeRecommendedAPIParent: toBeRecommendedAPIParents) {
                    boolean parentFoundInAPIRepresentations = false;
                    List<ApiRepresentation> apiRepresentationList = mapoApiFrequentSequencePattern.getPatternElement().getApiRepresentationList();
                    for( ApiRepresentation apiRepresentation : apiRepresentationList)
                        if( GRAAMBuilder.areSemanticallyTheSame(apiRepresentation.getFrameworkRelatedNode(), toBeRecommendedAPIParent ) ){
                            parentFoundInAPIRepresentations = true;
                            break;
                        }
                    if( !parentFoundInAPIRepresentations )
                        return;
                }
                for( DirectedGraphNode toBeRecommendedAPIChild: toBeRecommendedAPIChildren) {
                    boolean childFoundInAPIRepresentations = false;
                    List<ApiRepresentation> apiRepresentationList = mapoApiFrequentSequencePattern.getPatternElement().getApiRepresentationList();
                    for( ApiRepresentation apiRepresentation : apiRepresentationList)
                        if( GRAAMBuilder.areSemanticallyTheSame( apiRepresentation.getFrameworkRelatedNode(),  toBeRecommendedAPIChild ) ){
                            childFoundInAPIRepresentations = true;
                            break;
                        }
                    if( !childFoundInAPIRepresentations )
                        return;
                }
                candidatePatterns.add( mapoApiFrequentSequencePattern );

            } );
        } );
        return candidatePatterns;
    }

    double computePatternScore( FrequentSequencePattern<MapoApiCallSeq> mapoApiCallSeqFrequentSequencePattern, Iterable<DirectedGraphNode> toBeRecommendedAPIParents ){
        double similarity = 0;
        int matchCounter = 0;
        for(GRAAM graam: mapoApiCallSeqFrequentSequencePattern.getCorrespondingGraamSet()) {
           Map<DirectedGraphNode, DirectedGraphNode> graamNodeMap = findDirectedGraphNodeMappings( graam, toBeRecommendedAPIParents );
           for( DirectedGraphNode graamNode1: graamNodeMap.keySet() ) {
               similarity += LevenshteinSimilarityUtil.nameSimilarity(
                       ((FrameworkRelatedNode) graamNode1).getFrameworkRelatedClass()+"."+((FrameworkRelatedNode) graamNode1).getFrameworkRelatedMethod(),
                       ((FrameworkRelatedNode)graamNodeMap.get(graamNode1)).getFrameworkRelatedClass()+"."+((FrameworkRelatedNode)graamNodeMap.get(graamNode1)).getFrameworkRelatedMethod() );
               matchCounter++;
           }
       }
        return similarity / matchCounter;
    }

    Map<DirectedGraphNode, DirectedGraphNode> findDirectedGraphNodeMappings( GRAAM graam, Iterable<DirectedGraphNode> toBeMappedNodes ){
        Map<DirectedGraphNode, DirectedGraphNode> mappedDirectedGraphNodes = new HashMap<>();
        for(DirectedGraphNode graamNode: toBeMappedNodes){
            for(Object graamNode1: graam.getNodeSet()){
                if( GRAAMBuilder.areSemanticallyTheSame(graamNode,(DirectedGraphNode)graamNode1) ){
                    mappedDirectedGraphNodes.put( graamNode, (DirectedGraphNode)graamNode1 );
                    break;
                }
            }
        }
        return mappedDirectedGraphNodes;
    }

    public List<GRAAM> getTestGRAAMList() {
        return testGRAAMList;
    }

    public void setTestGRAAMList(List<GRAAM> testGRAAMList) {
        this.testGRAAMList = testGRAAMList;
    }

}
