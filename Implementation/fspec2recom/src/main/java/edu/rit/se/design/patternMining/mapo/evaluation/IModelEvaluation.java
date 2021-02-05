package edu.rit.se.design.patternMining.mapo.evaluation;

public interface IModelEvaluation< T extends IModelEvaluationResult> {
    public T evaluate();
}
