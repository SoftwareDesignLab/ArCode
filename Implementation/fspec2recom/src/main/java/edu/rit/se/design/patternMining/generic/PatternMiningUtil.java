package edu.rit.se.design.patternMining.generic;

import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.patternMining.frequentItemset.FrequentPattern;
import edu.rit.se.design.specminer.graam.GRAAM;
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
