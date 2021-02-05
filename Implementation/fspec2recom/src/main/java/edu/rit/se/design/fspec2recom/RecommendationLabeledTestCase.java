package edu.rit.se.design.fspec2recom;

import edu.rit.se.design.specminer.graam.GRAAM;

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
