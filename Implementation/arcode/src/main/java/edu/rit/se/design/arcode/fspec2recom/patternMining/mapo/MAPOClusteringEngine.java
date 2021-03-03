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

package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo;

import edu.rit.se.design.arcode.fspec2recom.clustering.ClassicalClusteringEngine;
import edu.rit.se.design.arcode.fspec2recom.clustering.ClusterNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MAPOClusteringEngine extends ClassicalClusteringEngine<MapoApiCallSeq> {
    protected double computeSimilarity( MapoApiCallSeq seq1, MapoApiCallSeq seq2 ){
        String seq1ClassName = seq1.getClassName();
        seq1ClassName = seq1ClassName.substring( seq1ClassName.lastIndexOf(".") + 1, seq1ClassName.length() );
        String seq2ClassName = seq2.getClassName();
        seq2ClassName = seq2ClassName.substring( seq2ClassName.lastIndexOf(".") + 1, seq2ClassName.length() );
        String seq1MethodName = seq1.getMethodName();
        seq1MethodName = seq1MethodName.substring( 0, seq1MethodName.indexOf("(") );
        String seq2MethodName = seq2.getMethodName();
        seq2MethodName = seq2MethodName.substring( 0, seq2MethodName.indexOf("(") );


        double similarity = apiSequencesSimilarity( seq1, seq2 ) + LevenshteinSimilarityUtil.nameSimilarity( seq1ClassName, seq2ClassName ) +
                LevenshteinSimilarityUtil.nameSimilarity( seq1MethodName, seq2MethodName );
        return similarity / 3;
    }

    @Override
    protected ClusterNode<MapoApiCallSeq> createClusterNodeForElement(MapoApiCallSeq element) {
        return new ClusterNodeApiSeq(element);
    }

    double apiSequencesSimilarity( MapoApiCallSeq seq1, MapoApiCallSeq seq2 ){
        List intersect = seq1.getApiRepresentationList().stream()
                .filter(seq2.getApiRepresentationList()::contains)
                .collect(Collectors.toList());

        List union = Stream.concat(seq1.getApiRepresentationList().stream(), seq2.getApiRepresentationList().stream())
                .distinct()
                .collect(Collectors.toList());
        return (1.0 * intersect.size()) / union.size();

    }


}
