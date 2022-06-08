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

package edu.rit.se.design.arcode.fspec2recom.patternMining.generic;

import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset.FrequentPattern;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;

public class PatternMiningUtil {

    public static int findAPISetPresenceFrequency( CodeRepository codeRepository, Set<ApiRepresentation> coAPISet ){
        int frequency = 0;

        for( String programName: codeRepository.getApiUsageInPrograms().keySet() ) {
            boolean allAPIsFound = true;
            Set<ApiRepresentation> programAPISet = codeRepository.getApiUsageInPrograms().get( programName );
            for (ApiRepresentation apiRepresentation : coAPISet)
                if( !programAPISet.contains( apiRepresentation ) ){
                    allAPIsFound = false;
                    break;
                }
            if (allAPIsFound)
                frequency++;
        }
        return frequency;
    }

    public static ImmutablePair<Integer, Set<GRAAM>> findAPISeqPresenceFrequency(CodeRepository codeRepository, ApiSequenceRepresentation apiSeqRep ){
        int frequency = 0;

        Set<GRAAM> graspSet = HashSetFactory.make();
        for( String programName: codeRepository.getApiSeqUsageInPrograms().keySet() ) {
            boolean allSeqFoundInProject = false;

            for (ApiSequenceRepresentation apiSequenceRepresentation : codeRepository.getApiSeqUsageInPrograms().get(programName)) {
                if (apiSequenceRepresentation.doesContain(apiSeqRep)) {
                    allSeqFoundInProject = true;
                    graspSet.add( apiSequenceRepresentation.getGraam() );
                }
            }
            if (allSeqFoundInProject)
                frequency++;
        }
        return new ImmutablePair<>( frequency, graspSet );
    }


    public static void eliminateBelowSupport(List<FrequentPattern> foundPatterns, int support ){
        List filteredFoundPatterns =  foundPatterns.stream().filter(foundPattern -> foundPattern.getFrequency() >= support ).collect(Collectors.toList());

/*
        int minSup = (int)(support * totalObservations);
        List filteredFoundPatterns =  foundPatterns.stream().filter(foundPattern -> foundPattern.getFrequency() >= minSup ).collect(Collectors.toList());
*/


        foundPatterns.clear();
        foundPatterns.addAll( filteredFoundPatterns );
    }


}
