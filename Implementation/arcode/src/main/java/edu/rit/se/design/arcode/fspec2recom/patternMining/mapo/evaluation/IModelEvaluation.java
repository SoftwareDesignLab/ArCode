package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;

public interface IModelEvaluation< T extends IModelEvaluationResult> {
    public T evaluate();
}
