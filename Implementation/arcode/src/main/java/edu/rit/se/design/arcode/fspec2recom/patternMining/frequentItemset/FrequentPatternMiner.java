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

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.Pattern;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.PatternMiner;

import java.util.*;

public abstract class FrequentPatternMiner<T extends FrequentPattern, G extends FrequentPatternSpecification> extends PatternMiner<T, G> {


    @Override
    protected List<T> findPatterns(CodeRepository codeRepository, List<T> foundPatterns, List<T> lastFoundPatterns, G frequentPatternSpecification) {
        if( foundPatterns.isEmpty() && lastFoundPatterns.isEmpty() )
            return generateInitialPatterns( codeRepository, frequentPatternSpecification );

        return generateNextLevelPatterns( codeRepository, foundPatterns, lastFoundPatterns, frequentPatternSpecification );
    }

    protected abstract List<T> generateInitialPatterns(CodeRepository codeRepository, G frequentPatternSpecification);
    protected abstract List<T> generateNextLevelPatterns(CodeRepository codeRepository, List<T> foundPatterns, List<T> lastFoundPatterns, G frequentPatternSpecification);

    public StringBuilder dotReport(){
        StringBuilder report = new StringBuilder( getClass().getSimpleName() + " DOT report:\n");
        getFoundPatterns().forEach(frequentPattern -> {
            report.append( "\t\"" + frequentPattern.report() + "\"" +  ";\n" );
            frequentPattern.getChildrenSet().forEach( frequentPatternChild -> {
                if( getFoundPatterns().contains( frequentPatternChild ) )
                    report.append( "\t\"" + frequentPattern.report() + "\" -> \"" + ((Pattern)frequentPatternChild).report() + "\";\n" );
            } );
        } );
        return report;
    }

}
