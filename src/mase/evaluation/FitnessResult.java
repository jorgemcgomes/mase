/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import mase.EvaluationResult;

/**
 *
 * @author jorge
 */
public class FitnessResult implements EvaluationResult {

    public static final int MAX = 0, MIN = 1, ARITHMETIC = 2, HARMONIC = 3, MIN_PLUS = 4;
    public static final float FLOAT_THRESHOLD = 0.0001f;
    protected Float value;
    protected int combination;

    public FitnessResult(Float value) {
        this.value = value;
        this.combination = HARMONIC;
    }

    public void setCombinationMethod(int combination) {
        this.combination = combination;
    }

    @Override
    public Float value() {
        return value;
    }

    @Override
    public FitnessResult mergeEvaluations(EvaluationResult[] results) {
        float score = 0;
        switch (combination) {
            case MAX:
                score = Float.NEGATIVE_INFINITY;
                for (EvaluationResult f : results) {
                    score = Math.max(score, (Float) f.value());
                }
                break;
            case MIN:
                score = Float.POSITIVE_INFINITY;
                for (EvaluationResult f : results) {
                    score = Math.min(score, (Float) f.value());
                }
                break;
            case ARITHMETIC:
                for (EvaluationResult f : results) {
                    score += (Float) f.value();
                }
                score /= results.length;
                break;
            case HARMONIC:
                for (EvaluationResult f : results) {
                    Float val = (Float) f.value();
                    if (val > -FLOAT_THRESHOLD && val < FLOAT_THRESHOLD) {
                        score += 1 / FLOAT_THRESHOLD;
                    } else {
                        score += 1 / val;
                    }
                }
                score = results.length / score;
                break;
            case MIN_PLUS:
                float min = Float.MAX_VALUE;
                float mean = 0;
                for (EvaluationResult f : results) {
                    mean += (Float) f.value();
                    min = Math.min(min, (Float) f.value());
                }
                score = 0.01f * (mean / results.length) + 0.99f * min;
                break;
        }
        FitnessResult fit = new FitnessResult(score);
        fit.setCombinationMethod(combination);
        return fit;
    }

    @Override
    public String toString() {
        return value + "";
    }
    
    
}
