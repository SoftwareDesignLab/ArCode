package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;

public interface IModelEvaluationResult<E extends IModelEvaluationResult> {
    String getEvaluationResult();
    public void mergeResults(E modelEvaluationResult);
}
