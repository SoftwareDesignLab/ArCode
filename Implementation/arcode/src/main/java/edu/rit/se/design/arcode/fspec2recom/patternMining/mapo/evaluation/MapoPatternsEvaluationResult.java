package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;

import edu.rit.se.design.arcode.fspec2recom.patternMining.frequentItemset.FrequentSequencePattern;
import edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.MapoApiCallSeq;

import java.util.List;
import java.util.Map;

public class MapoPatternsEvaluationResult implements IModelEvaluationResult {
    long patternExtractionTime;
    Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns;
    double minSupport;
    int totalClusters;

    public MapoPatternsEvaluationResult(Map<String, List<FrequentSequencePattern<MapoApiCallSeq>>> foundPatterns, long patternExtractionTime, int totalClusters, double minSupport){
        this.patternExtractionTime = patternExtractionTime;
        this.foundPatterns = foundPatterns;
        this.totalClusters = totalClusters;
        this.minSupport = minSupport;
    }

    @Override
    public String getEvaluationResult() {
        StringBuilder report = new StringBuilder();
        report.append( "Found patterns in " + totalClusters + " clusters with min support of " + minSupport + " in " + (patternExtractionTime) + " ms:" ) ;
        foundPatterns.forEach((s, frequentSequencePatterns) -> {
            report.append( "\nCluster " + s + ": " );
            frequentSequencePatterns.forEach( mapoApiCallSeqFrequentSequencePattern ->
                    report.append("\n\t" + mapoApiCallSeqFrequentSequencePattern.report()) );
//            System.out.println("");
        });

        return report.toString();
    }

    @Override
    public void mergeResults(IModelEvaluationResult modelEvaluationResult) {

    }
}
