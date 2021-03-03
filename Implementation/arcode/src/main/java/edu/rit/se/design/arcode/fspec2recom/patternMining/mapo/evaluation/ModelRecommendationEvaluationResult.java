package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract public class ModelRecommendationEvaluationResult<E extends ModelRecommendationEvaluationResult> implements IModelEvaluationResult<E> {
    int totalExperiment = 0;
    int totalMissed = 0;
    int totalHit = 0;
    int totalTestGRAAMs = 0;
    int totalModelNodes = 0;
    int totalModelEdges = 0;
    Map<Integer, Integer> rankedSolutionMap = new HashMap<>();

    public void increaseTotalExperiment(){
        totalExperiment++;
    }

    public void increaseTotalHit(int foundSolutionRank){
        totalHit++;
        addRankedSolution( foundSolutionRank );
    }

    void addRankedSolution( Integer solutionRank ){
        if( !rankedSolutionMap.containsKey(solutionRank) )
            rankedSolutionMap.put( solutionRank, 0 );
        rankedSolutionMap.put( solutionRank, rankedSolutionMap.get( solutionRank ) + 1 );
    }

    public void increaseTotalMissed(){
        totalMissed++;
    }

    public void increaseTotalTestGRAAMs(){
        totalTestGRAAMs++;
    }

    public void setTotalModelNodes(int totalModelNodes){
        this.totalModelNodes = totalModelNodes;
    }

    public void setTotalModelEdges(int totalModelEdges){
        this.totalModelEdges = totalModelEdges;
    }

    public int getTotalExperiment() {
        return totalExperiment;
    }

    public void setTotalExperiment(int totalExperiment) {
        this.totalExperiment = totalExperiment;
    }

    public int getTotalMissed() {
        return totalMissed;
    }

    public void setTotalMissed(int totalMissed) {
        this.totalMissed = totalMissed;
    }

    public int getTotalHit() {
        return totalHit;
    }

    public void setTotalHit(int totalHit) {
        this.totalHit = totalHit;
    }

    public int getTotalTestGRAAMs() {
        return totalTestGRAAMs;
    }

    public void setTotalTestGRAAMs(int totalTestGRAAMs) {
        this.totalTestGRAAMs = totalTestGRAAMs;
    }

    public int getTotalModelNodes() {
        return totalModelNodes;
    }

    public int getTotalModelEdges() {
        return totalModelEdges;
    }

    public Map<Integer, Integer> getRankedSolutionMap() {
        return rankedSolutionMap;
    }

    public void setRankedSolutionMap(Map<Integer, Integer> rankedSolutionMap) {
        this.rankedSolutionMap = rankedSolutionMap;
    }

    public void mergeResults(E modelRecommendationEvaluationResult){
        setTotalModelEdges( getTotalModelEdges() + modelRecommendationEvaluationResult.getTotalModelEdges() );
        setTotalModelNodes( getTotalModelNodes() + modelRecommendationEvaluationResult.getTotalModelNodes() );
        setTotalExperiment( getTotalExperiment() + modelRecommendationEvaluationResult.getTotalExperiment() );
        setTotalHit( getTotalHit() + modelRecommendationEvaluationResult.getTotalHit() );
        setTotalMissed( getTotalMissed() + modelRecommendationEvaluationResult.getTotalMissed() );
        setTotalTestGRAAMs( getTotalTestGRAAMs() + modelRecommendationEvaluationResult.getTotalTestGRAAMs() );
        modelRecommendationEvaluationResult.getRankedSolutionMap().keySet().forEach( solutionRank -> {
            if( getRankedSolutionMap().get( solutionRank ) == null )
                getRankedSolutionMap().put((Integer) solutionRank, 0 );
            getRankedSolutionMap().put( (Integer)solutionRank, (Integer)modelRecommendationEvaluationResult.getRankedSolutionMap().get( solutionRank ) +
                    getRankedSolutionMap().get( solutionRank ) );
        } );
    }

    protected abstract String getEvaluationName();

    @Override
    public String getEvaluationResult() {
        return getEvaluationName() + " Result:" +
                "\n\tTotal Evaluated GRAAMs: " + totalTestGRAAMs+
                "\n\tTotal Experiments: " + totalExperiment +
                "\n\tTotal Correct Recommendations: " + totalHit +
                "\n\tAverage Ranking of Recommendations: " + getAverageSolutionRankReport() +
                "\n\tDetailed Ranking of Recommendations: " + getDetailedSolutionRankReport() +
                "\n\tCumulative Ranking of Recommendations: " + getDetailedSolutionCumulativeRankReport() +

                "\n\tTotal Incorrect Recommendations: " + totalMissed+
                "\n\tAccuracy: %" + new DecimalFormat("#.##").format(getAccuracy());
    }

    public double getAccuracy(){
        return 100.0 * totalHit / totalExperiment;
    }

    String getAverageSolutionRankReport(){
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if( totalHit == 0 )
            return decimalFormat.format(0);
        int totalRank = 0;
        for( Integer solutionRank: getRankedSolutionMap().keySet() ){
            totalRank += getRankedSolutionMap().get( solutionRank ) * solutionRank;
        }
        return decimalFormat.format(1.0 * totalRank / totalHit/*totalExperiment*/);
    }

    String getDetailedSolutionRankReport(){
        String report = "";
        if( getRankedSolutionMap().isEmpty() )
            return report;
        Optional<Integer> maxRank = getRankedSolutionMap().keySet().stream().max((o1, o2) -> o1.compareTo(o2));

        for( int solutionRank = 1; solutionRank <= maxRank.get(); solutionRank++ ){
            double rankVal = getRankedSolutionMap().get( solutionRank ) == null ? 0 : getRankedSolutionMap().get( solutionRank );
            report += "\t" + new DecimalFormat("#.##").format(100.0 * rankVal / /*totalHit*/totalExperiment);
        }
        return report;
    }

    String getDetailedSolutionCumulativeRankReport(){
        String report = "";
        if( getRankedSolutionMap().isEmpty() )
            return report;
        Optional<Integer> maxRank = getRankedSolutionMap().keySet().stream().max((o1, o2) -> o1.compareTo(o2));

        double cumulativeRank = 0;
        for( int solutionRank = 1; solutionRank <= maxRank.get(); solutionRank++ ){
            double rankVal = getRankedSolutionMap().get( solutionRank ) == null ? 0 : getRankedSolutionMap().get( solutionRank );
            cumulativeRank += 100.0 * rankVal / /*totalHit*/totalExperiment;
            report += "\t" + new DecimalFormat("#.##").format(cumulativeRank);
        }
        return report;
    }

}
