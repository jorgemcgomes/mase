/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

/**
 *
 * @author jorge
 */
public class FitnessResult implements EvaluationResult {

    private static final long serialVersionUID = 1;
    public static final int MAX = 0, MIN = 1, ARITHMETIC = 2, HARMONIC = 3, MIN_PLUS = 4;
    public static final double FLOAT_THRESHOLD = 0.0001f;
    protected double value;
    protected int combination;

    public FitnessResult(double value) {
        this(value, HARMONIC);
    }

    public FitnessResult(double value, int combination) {
        this.value = value;
        this.combination = combination;
    }

    @Override
    public Double value() {
        return value;
    }

    @Override
    public FitnessResult mergeEvaluations(EvaluationResult[] results) {
        double score = 0;
        switch (combination) {
            case MAX:
                score = Double.NEGATIVE_INFINITY;
                for (EvaluationResult f : results) {
                    score = Math.max(score, (Double) f.value());
                }
                break;
            case MIN:
                score = Double.POSITIVE_INFINITY;
                for (EvaluationResult f : results) {
                    score = Math.min(score, (Double) f.value());
                }
                break;
            case ARITHMETIC:
                for (EvaluationResult f : results) {
                    score += (Double) f.value();
                }
                score /= results.length;
                break;
            case HARMONIC:
                for (EvaluationResult f : results) {
                    Double val = (Double) f.value();
                    if (val > -FLOAT_THRESHOLD && val < FLOAT_THRESHOLD) {
                        score += 1 / FLOAT_THRESHOLD;
                    } else {
                        score += 1 / val;
                    }
                }
                score = results.length / score;
                break;
            case MIN_PLUS:
                double min = Double.MAX_VALUE;
                double mean = 0;
                for (EvaluationResult f : results) {
                    mean += (Double) f.value();
                    min = Math.min(min, (Double) f.value());
                }
                score = 0.01f * (mean / results.length) + 0.99f * min;
                break;
        }
        FitnessResult fit = new FitnessResult(score, combination);
        return fit;
    }

    @Override
    public String toString() {
        return value + "";
    }

}
