package edu.rit.se.design.patternMining.generic;

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
