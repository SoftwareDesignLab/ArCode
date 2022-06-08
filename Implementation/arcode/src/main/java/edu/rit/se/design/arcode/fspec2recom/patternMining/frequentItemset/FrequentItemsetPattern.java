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

import java.util.Set;

public class FrequentItemsetPattern<T> extends FrequentPattern<Set<T>> {


    public FrequentItemsetPattern(int frequency, Set<T> patternElement) {
        super(frequency, patternElement);
    }

    public boolean equals(Object obj ){
        if( obj == null || !( obj instanceof FrequentItemsetPattern) )
            return false;
        FrequentItemsetPattern<T> other = (FrequentItemsetPattern<T>) obj;

        return other.getPatternElement().equals( this.getPatternElement() );
    }

    public int hashCode(){
        return 0;
    }

//    public boolean isClosedSet(){
//        for( FrequentItemsetPattern child: getChildrenSet() )
//            if( child.getFrequency() >= getFrequency() )
//                return false;
//        return true;
//    }

    @Override
    public StringBuilder report() {
        StringBuilder report = new StringBuilder();
        getPatternElement().forEach( apiRepresentation -> report.append( (report.length() > 0 ? ", " : "") + apiRepresentation ) );
//        report.insert(0, "Length: " + getItemSet().size() + ", Freq: " + getFrequency() + ", [" );
        report.insert(0, getFrequency() + "_[" );

        report.append("]");
        return report;
    }
}
