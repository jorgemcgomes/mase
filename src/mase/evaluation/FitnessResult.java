/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.Collection;
import java.util.Locale;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author jorge
 */
public class FitnessResult implements EvaluationResult<Double> {

    private static final long serialVersionUID = 1;
    public static final double FLOAT_THRESHOLD = 0.0001f;
    protected double value;
    protected MergeMode average;

    public enum MergeMode {
        max, min, arithmetic, harmonic, minPlus, median
    }

    public FitnessResult(double value) {
        this(value, MergeMode.arithmetic);
    }

    public FitnessResult(double value, MergeMode average) {
        this.value = value;
        this.average = average;
    }

    @Override
    public Double value() {
        return value;
    }

    public MergeMode getAverageType() {
        return average;
    }

    @Override
    public FitnessResult mergeEvaluations(Collection<EvaluationResult<Double>> results) {
        double score = 0;
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (EvaluationResult<Double> f : results) {
            ds.addValue(f.value());
        }
        switch (average) {
            case max:
                score = ds.getMax();
                break;
            case min:
                score = ds.getMin();
                break;
            case arithmetic:
                score = ds.getMean();
                break;
            case harmonic:
                for (EvaluationResult f : results) {
                    Double val = (Double) f.value();
                    if (val > -FLOAT_THRESHOLD && val < FLOAT_THRESHOLD) {
                        score += 1 / FLOAT_THRESHOLD;
                    } else {
                        score += 1 / val;
                    }
                }
                score = results.size() / score;
                break;
            case minPlus:
                double min = ds.getMin();
                double mean = ds.getMean();
                score = 0.01f * (mean / results.size()) + 0.99f * min;
                break;
            case median:
                ds.setMeanImpl(new Median());
                score = ds.getMean();
                break;
        }
        FitnessResult fit = new FitnessResult(score, average);
        fit.average = this.average;
        return fit;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%.5f", value);
    }

}
