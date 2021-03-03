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

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.PatternSpecification;

public class FrequentPatternSpecification extends PatternSpecification {
    int support;
    boolean closedFrequent;

    public FrequentPatternSpecification(int support, boolean closedFrequent){
        setSupport( support );
        setClosedFrequent(closedFrequent);
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public boolean isClosedFrequent() {
        return closedFrequent;
    }

    public void setClosedFrequent(boolean closedFrequent) {
        this.closedFrequent = closedFrequent;
    }
}
