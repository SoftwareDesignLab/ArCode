package edu.rit.se.design.patternMining.frequentItemset;

import edu.rit.se.design.patternMining.generic.CodeRepository;
import edu.rit.se.design.patternMining.generic.Pattern;
import edu.rit.se.design.patternMining.generic.PatternMiner;

import java.util.*;

public abstract class FrequentPatternMiner<T extends FrequentPattern, G extends FrequentPatternSpecification> extends PatternMiner<T, G> {


    @Override
    protected List<T> findPatterns(CodeRepository codeRepository, List<T> foundPatterns, List<T> lastFoundPatterns, G frequentPatternSpecification) {
        if( foundPatterns.isEmpty() && lastFoundPatterns.isEmpty() )
            return generateInitialPatterns( codeRepository, frequentPatternSpecification );

        return generateNextLevelPatterns( codeRepository, foundPatterns, lastFoundPatterns, frequentPatternSpecification );
    }

    protected abstract List<T> generateInitialPatterns(CodeRepository codeRepository, G frequentPatternSpecification);
    protected abstract List<T> generateNextLevelPatterns(CodeRepository codeRepository, List<T> foundPatterns, List<T> lastFoundPatterns, G frequentPatternSpecification);

    public StringBuilder dotReport(){
        StringBuilder report = new StringBuilder( getClass().getSimpleName() + " DOT report:\n");
        getFoundPatterns().forEach(frequentPattern -> {
            report.append( "\t\"" + frequentPattern.report() + "\"" +  ";\n" );
            frequentPattern.getChildrenSet().forEach( frequentPatternChild -> {
                if( getFoundPatterns().contains( frequentPatternChild ) )
                    report.append( "\t\"" + frequentPattern.report() + "\" -> \"" + ((Pattern)frequentPatternChild).report() + "\";\n" );
            } );
        } );
        return report;
    }

}
