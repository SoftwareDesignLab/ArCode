package edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset;

import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.Pattern;

import java.util.HashSet;
import java.util.Set;

public abstract class FrequentPattern<T> implements Pattern {
    int frequency;
    T patternElement;
    Set<FrequentPattern> childrenSet;

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public FrequentPattern(int frequency, T patternElement){
        setFrequency(frequency);
        setPatternElement( patternElement );
        setChildrenSet( new HashSet<>());
    }

    public void addChild(FrequentPattern child ){
        getChildrenSet().add( child );
    }

    public Set<FrequentPattern> getChildrenSet() {
        return childrenSet;
    }

    public void setChildrenSet(Set<FrequentPattern> childrenSet) {
        this.childrenSet = childrenSet;
    }

    public T getPatternElement() {
        return patternElement;
    }

    public void setPatternElement(T patternElement) {
        this.patternElement = patternElement;
    }
}
