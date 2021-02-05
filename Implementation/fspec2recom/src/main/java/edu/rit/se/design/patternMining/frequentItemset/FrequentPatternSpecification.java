package edu.rit.se.design.patternMining.frequentItemset;

import edu.rit.se.design.patternMining.generic.PatternSpecification;

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
