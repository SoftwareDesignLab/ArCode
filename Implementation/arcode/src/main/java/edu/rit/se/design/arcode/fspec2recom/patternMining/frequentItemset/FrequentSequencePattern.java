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


import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.ApiSequenceRepresentation;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMVisualizer;

import java.util.Set;

public class FrequentSequencePattern<T extends ApiSequenceRepresentation> extends FrequentPattern<T> {

    Set<GRAAM> correspondingGraamSet;

    public FrequentSequencePattern(int frequency, T patternElement, Set<GRAAM> correspondingGraamSet) {
        super(frequency, patternElement);
        setCorrespondingGraamSet(correspondingGraamSet);
    }

    public Set<GRAAM> getCorrespondingGraamSet() {
        return correspondingGraamSet;
    }

    public void setCorrespondingGraamSet(Set<GRAAM> correspondingGraamSet) {
        this.correspondingGraamSet = correspondingGraamSet;
    }

    public boolean equals(Object obj ){
        if( obj == null || !( obj instanceof FrequentSequencePattern) )
            return false;
        FrequentSequencePattern<T> other = (FrequentSequencePattern<T>) obj;

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
        ((ApiSequenceRepresentation)getPatternElement()).getApiRepresentationList().forEach( apiRepresentation -> report.append( (report.length() > 0 ? " → " : "") + apiRepresentation ) );
//        report.insert(0, "Length: " + getItemSet().size() + ", Freq: " + getFrequency() + ", [" );
        report.insert(0, getFrequency() + "_[" );

        report.append("]");
        getCorrespondingGraamSet().forEach( graam -> report.append( "\n\t\t" + new GRAAMVisualizer(graam).dotOutput()) );
        return report;
    }
}
