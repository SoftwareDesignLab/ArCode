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

import java.util.ArrayList;
import java.util.List;

public abstract class PatternMiner<T extends Pattern, E extends PatternSpecification> {
    List<T> foundPatterns;

    public void findPatterns(CodeRepository codeRepository , E patternSpecification){
        List<T> lastFoundPatterns = new ArrayList<>();
        List<T> foundPatterns = new ArrayList<>();

        do {
            foundPatterns.addAll( lastFoundPatterns );
            lastFoundPatterns = findPatterns(codeRepository, foundPatterns, lastFoundPatterns, patternSpecification );
        }while( morePatternsAvailable( foundPatterns, lastFoundPatterns ) );

        this.foundPatterns = foundPatterns;
    }

    public List<T> getFoundPatterns(){
        return foundPatterns;
    }

    protected abstract List<T> findPatterns(CodeRepository codeRepository, List<T> foundPatterns, List<T> lastFoundPatterns, E patternSpecification);

    protected boolean morePatternsAvailable(List<T> foundPatterns, List<T> lastFoundPatterns){
        return !foundPatterns.containsAll( lastFoundPatterns );
    }

    public StringBuilder report(){
        StringBuilder report = new StringBuilder();
        report.append( this.getClass().getSimpleName() + " report:\n");
        getFoundPatterns().forEach(pattern -> {
            report.append( "\t" + pattern.report() + "\n" );
        } );
        return report;
    }

}
