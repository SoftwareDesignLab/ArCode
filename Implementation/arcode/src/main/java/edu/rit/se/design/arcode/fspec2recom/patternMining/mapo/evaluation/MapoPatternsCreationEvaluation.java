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

import edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset.FrequentSequencePattern;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MAPOClusteringEngine;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MapoApiCallSeq;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MapoApiSeqCallMiner;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MapoCodeAnalyzer;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspec2recom.clustering.ClusterNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapoPatternsCreationEvaluation implements IModelEvaluation<MapoPatternsEvaluationResult> {
    Map<String, List<GRAAM>> graamProjectMap;
    int totalClusters;
    double minSupport;
    Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns;

    public MapoPatternsCreationEvaluation(Map<String, List<GRAAM>> graamProjectMap, int totalClusters, double minSupport ){
        this.graamProjectMap = graamProjectMap;
        this.totalClusters = totalClusters;
        this.minSupport = minSupport;
        foundPatterns = new HashMap<>();
    }

    @Override
    public MapoPatternsEvaluationResult evaluate() {
        foundPatterns.clear();
        long timer = System.currentTimeMillis();
        CodeRepository codeRepository = new CodeRepository(graamProjectMap);
        MapoCodeAnalyzer mapoCodeAnalyzer = new MapoCodeAnalyzer( codeRepository );

        MAPOClusteringEngine mapoClusteringEngine = new MAPOClusteringEngine();

        ClusterNode cluster =  mapoClusteringEngine.binaryCluster( mapoCodeAnalyzer.getAllMapoApiCallSequences() );
        List<ClusterNode> clusters = mapoClusteringEngine.toClusters( cluster, totalClusters );
//        clusters.forEach( clusterNode -> System.out.println( clusterNode.toString() ) );

        MapoApiSeqCallMiner mapoApiSeqCallMiner = new MapoApiSeqCallMiner();

        mapoApiSeqCallMiner.findPatterns(clusters, minSupport);

        foundPatterns = mapoApiSeqCallMiner.getFoundPatterns();

        return new MapoPatternsEvaluationResult( foundPatterns,
                System.currentTimeMillis() - timer, totalClusters, minSupport);

//        System.out.println( "Finding MAPO patterns finished in " + ((System.currentTimeMillis() - timer ) ) + " milli seconds" );



    }

    public Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> getFoundPatterns() {
        return foundPatterns;
    }

    public void setFoundPatterns(Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns) {
        this.foundPatterns = foundPatterns;
    }
}
