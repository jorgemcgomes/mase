/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.Locale;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author jorge
 */
public class FitnessResult implements EvaluationResult<Double> {

    private static final long serialVersionUID = 1;
    protected double value;
    protected FitnessResultMerger merger;

    public FitnessResult(double value) {
        this(value, FitnessResultMerger.ARITHMETIC);
    }

    public FitnessResult(double value, int average) {
        this.value = value;
        this.merger = new FitnessResultMerger(average);
    }

    @Override
    public Double value() {
        return value;
    }


    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%.5f", value);
    }

    @Override
    public EvaluationResultMerger getResultMerger() {
        return merger;
    }

    public static class FitnessResultMerger implements EvaluationResultMerger {

        public static final int MAX = 0, MIN = 1, ARITHMETIC = 2, HARMONIC = 3, MIN_PLUS = 4, MEDIAN = 5;
        public static final double FLOAT_THRESHOLD = 0.0001f;
        private int average;

        public FitnessResultMerger(int average) {
            this.average = average;
        }

        @Override
        public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
            double score = 0;
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (EvaluationResult f : results) {
                ds.addValue(((FitnessResult) f).value());
            }
            switch (average) {
                case MAX:
                    score = ds.getMax();
                    break;
                case MIN:
                    score = ds.getMin();
                    break;
                case ARITHMETIC:
                    score = ds.getMean();
                    break;
                case HARMONIC:
                    for (EvaluationResult f : results) {
                        double val = ((FitnessResult) f).value();
                        if (val > -FLOAT_THRESHOLD && val < FLOAT_THRESHOLD) {
                            score += 1 / FLOAT_THRESHOLD;
                        } else {
                            score += 1 / val;
                        }
                    }
                    score = results.length / score;
                    break;
                case MIN_PLUS:
                    double min = ds.getMin();
                    double mean = ds.getMean();
                    score = 0.01f * (mean / results.length) + 0.99f * min;
                    break;
                case MEDIAN:
                    ds.setMeanImpl(new Median());
                    score = ds.getMean();
                    break;
            }
            return new FitnessResult(score);
        }

    }

}
