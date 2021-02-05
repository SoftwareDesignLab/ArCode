package edu.rit.se.design.patternMining.frequentItemset;

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
