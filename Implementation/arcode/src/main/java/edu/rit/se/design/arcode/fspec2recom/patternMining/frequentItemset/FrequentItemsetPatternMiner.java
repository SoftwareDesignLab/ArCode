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
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.PatternMiningUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrequentItemsetPatternMiner<T extends ApiRepresentation> extends FrequentPatternMiner<FrequentItemsetPattern<T>, FrequentPatternSpecification> {

    @Override
    protected List<FrequentItemsetPattern<T>> generateInitialPatterns(CodeRepository codeRepository, FrequentPatternSpecification frequentPatternSpecification) {
        List foundPatterns = new ArrayList<>();
        Set<T> apiRepresentationSet = (Set<T>) codeRepository.getApiSetInRepository();

        apiRepresentationSet.forEach( t -> {
            Set<T> itemset = new HashSet<>();
            itemset.add( t );
            int frequency = PatternMiningUtil.findAPISetPresenceFrequency( codeRepository, (Set<ApiRepresentation>) itemset);
            foundPatterns.add( new FrequentItemsetPattern(frequency, itemset) );
        } );

        PatternMiningUtil.eliminateBelowSupport( foundPatterns, frequentPatternSpecification.getSupport() );
        return foundPatterns;
    }

    @Override
    protected List<FrequentItemsetPattern<T>> generateNextLevelPatterns(CodeRepository codeRepository, List<FrequentItemsetPattern<T>> foundPatterns, List<FrequentItemsetPattern<T>> lastFoundPatterns, FrequentPatternSpecification frequentPatternSpecification) {
        Set<FrequentItemsetPattern<T>> toBeRemovedPatterns = new HashSet<>();
        Set<FrequentItemsetPattern<T>> newFoundPatterns = new HashSet<>();
        for( int i = 0; i < lastFoundPatterns.size(); i++ )
            for( int j = i+1; j < lastFoundPatterns.size(); j++ ){
                Set<T> itemset = new HashSet<>();
                itemset.addAll( lastFoundPatterns.get(i).getPatternElement() );
                itemset.addAll( lastFoundPatterns.get(j).getPatternElement() );

                // We want to gradually create new patterns. So we only find patterns that their itemset size is equal
                // to their parents itemset size plus 1.
                if( itemset.size() != lastFoundPatterns.get(i).getPatternElement().size() + 1 ||
                        itemset.size() != lastFoundPatterns.get(j).getPatternElement().size() + 1)
                    continue;

                int frequency = PatternMiningUtil.findAPISetPresenceFrequency( codeRepository, (Set<ApiRepresentation>) itemset);
                if( frequency < frequentPatternSpecification.getSupport() )
                    continue;
                FrequentItemsetPattern<T> frequentItemsetPattern = new FrequentItemsetPattern<T>(frequency, itemset);

                lastFoundPatterns.get(i).addChild(frequentItemsetPattern);
                lastFoundPatterns.get(j).addChild(frequentItemsetPattern);

                newFoundPatterns.add(frequentItemsetPattern);

                if( frequentPatternSpecification.isClosedFrequent() ) {
                    if (lastFoundPatterns.get(i).getFrequency() == frequency)
                        toBeRemovedPatterns.add(lastFoundPatterns.get(i));
                    if (lastFoundPatterns.get(j).getFrequency() == frequency)
                        toBeRemovedPatterns.add(lastFoundPatterns.get(j));
                }

            }

        foundPatterns.removeAll( toBeRemovedPatterns );
        return new ArrayList<>( newFoundPatterns );
    }



}
