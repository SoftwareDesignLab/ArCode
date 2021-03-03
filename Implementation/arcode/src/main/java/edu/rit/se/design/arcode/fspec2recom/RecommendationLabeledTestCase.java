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

package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class RecommendationLabeledTestCase {
    GRAAM originalGraam;
    GRAAM testGraam;
    GraphEditDistanceInfo recommendation;

    public RecommendationLabeledTestCase(GRAAM originalGraam, GRAAM testGraam) {
        this.originalGraam = originalGraam;
        this.testGraam = testGraam;
    }

    public void setRecommendation(GraphEditDistanceInfo recommendation) {
        this.recommendation = recommendation;
    }
}
