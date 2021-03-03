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

package edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset;

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.ApiRepresentation;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.ApiSequenceRepresentation;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.PatternMiningUtil;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class FrequentSequencePatternMiner<T extends ApiSequenceRepresentation> extends FrequentPatternMiner<FrequentSequencePattern<T>, FrequentPatternSpecification> {
    @Override
    protected List<FrequentSequencePattern<T>> generateInitialPatterns(CodeRepository codeRepository, FrequentPatternSpecification frequentPatternSpecification) {

        List foundPatterns = new ArrayList<>();
        List<ApiRepresentation> apiRepresentationList = new ArrayList<>(codeRepository.getApiSetInRepository());

        for (int i = 0; i < apiRepresentationList.size(); i++)
            for (int j = 0; j < apiRepresentationList.size(); j++) {
                T apiSequenceRepresentation = getApiSequenceRepresentationInstance();
                apiSequenceRepresentation.addToLast(apiRepresentationList.get(i));
                apiSequenceRepresentation.addToLast(apiRepresentationList.get(j));

                ImmutablePair<Integer, Set<GRAAM>> freqGraspPair = PatternMiningUtil.findAPISeqPresenceFrequency(codeRepository, apiSequenceRepresentation);
                foundPatterns.add(new FrequentSequencePattern<T>(freqGraspPair.getLeft(), apiSequenceRepresentation, freqGraspPair.getRight()));
            }

        PatternMiningUtil.eliminateBelowSupport(foundPatterns, frequentPatternSpecification.getSupport());
        return foundPatterns;
    }

    protected abstract T getApiSequenceRepresentationInstance();

    @Override
    protected List<FrequentSequencePattern<T>> generateNextLevelPatterns(CodeRepository codeRepository, List<FrequentSequencePattern<T>> foundPatterns, List<FrequentSequencePattern<T>> lastFoundPatterns, FrequentPatternSpecification frequentPatternSpecification) {
        Set<FrequentSequencePattern<T>> toBeRemovedPatterns = new HashSet<>();
        Set<FrequentSequencePattern<T>> newFoundPatterns = new HashSet<>();
        for( int i = 0; i < lastFoundPatterns.size(); i++ ){
            T lastFoundApiSequenceRepresentation = lastFoundPatterns.get(i).getPatternElement();
            int finalI = i;
            codeRepository.getApiSetInRepository().forEach(apiRepresentation -> {
                T newSeqCandidate1 = (T) lastFoundApiSequenceRepresentation.clone();
                newSeqCandidate1.addToLast( apiRepresentation );

                T newSeqCandidate2 = (T) lastFoundApiSequenceRepresentation.clone();
                newSeqCandidate2.addAtFirst( apiRepresentation );

                Set<T> itemset1 = new HashSet<>();
                Set<T> itemset2 = new HashSet<>();

                itemset1.add( newSeqCandidate1 );
                itemset2.add( newSeqCandidate2 );

                ImmutablePair<Integer, Set<GRAAM>> freqGraspPair1 = PatternMiningUtil.findAPISeqPresenceFrequency( codeRepository, newSeqCandidate1 );
                if( freqGraspPair1.getLeft() >= frequentPatternSpecification.getSupport() ) {
                    FrequentSequencePattern newPattern = new FrequentSequencePattern(freqGraspPair1.getLeft(), newSeqCandidate1, freqGraspPair1.getRight());
                    newFoundPatterns.add(newPattern);
                    lastFoundPatterns.get(finalI).getChildrenSet().add( newPattern );
                    if (freqGraspPair1.getLeft() >= lastFoundPatterns.get(finalI).getFrequency())
                        toBeRemovedPatterns.add(lastFoundPatterns.get(finalI));
                }

                ImmutablePair<Integer, Set<GRAAM>> freqGraspPair2 = PatternMiningUtil.findAPISeqPresenceFrequency( codeRepository, newSeqCandidate2 );
                if( freqGraspPair2.getLeft() >= frequentPatternSpecification.getSupport() ) {
                    FrequentSequencePattern newPattern = new FrequentSequencePattern(freqGraspPair2.getLeft(), newSeqCandidate2, freqGraspPair2.getRight());
                    newFoundPatterns.add(newPattern);
                    lastFoundPatterns.get(finalI).getChildrenSet().add( newPattern );
                    if (freqGraspPair2.getLeft() >= lastFoundPatterns.get(finalI).getFrequency())
                        toBeRemovedPatterns.add(lastFoundPatterns.get(finalI));
                }
            } );
        }

//        PatternMiningUtil.eliminateBelowSupport( newFoundPatterns, patternSpecification.getSupport() );
        foundPatterns.removeAll( toBeRemovedPatterns );
        return new ArrayList<>( newFoundPatterns );    }

}