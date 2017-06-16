/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import mase.util.Input;
import mase.util.Output;
import mase.util.Point;
import mase.util.WeightedPoint;
import mase.util.WeiszfeldAlgorithm;
import net.jafama.FastMath;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author jorge
 */
public class VectorBehaviourResult implements BehaviourResult<double[]> {

    private static final long serialVersionUID = 1;
    protected double[] behaviour;
    protected Distance dist;
    protected LocationEstimator estimator;

    public enum Distance {
        cosine, brayCurtis, euclidean, manhattan
    };

    public enum LocationEstimator {
        mean, cwMedian, geometricMedian, all
    };

    public VectorBehaviourResult(double... bs) {
        this.behaviour = bs;
        this.dist = Distance.euclidean;
        this.estimator = LocationEstimator.mean;
    }

    public void setDistance(Distance dist) {
        this.dist = dist;
    }

    public void setLocationEstimator(LocationEstimator est) {
        this.estimator = est;
    }

    @Override
    public double distanceTo(BehaviourResult other) {
        return vectorDistance(behaviour, (double[]) other.value());
    }

    @Override
    public double[] value() {
        return getBehaviour();
    }

    public void setBehaviour(double[] b) {
        this.behaviour = b;
    }

    public double[] getBehaviour() {
        return this.behaviour;
    }

    @Override
    public VectorBehaviourResult mergeEvaluations(Collection<EvaluationResult<double[]>> results) {
        double[] merged = new double[behaviour.length];
        switch (estimator) {
            case mean:
                Arrays.fill(merged, 0f);
                for (int i = 0; i < merged.length; i++) {
                    for (EvaluationResult<double[]> r : results) {
                        merged[i] += r.value()[i];
                    }
                    merged[i] /= results.size();
                }
                break;
            case cwMedian:
                DescriptiveStatistics ds = new DescriptiveStatistics();
                ds.setMeanImpl(new Median());
                for (int i = 0; i < merged.length; i++) {
                    for (EvaluationResult<double[]> r : results) {
                        ds.addValue(r.value()[i]);
                    }
                    merged[i] = ds.getMean();
                    ds.clear();
                }
                break;
            case geometricMedian:
                List<WeightedPoint> wps = new ArrayList<>(results.size());
                for (EvaluationResult<double[]> r : results) {
                    WeightedPoint wp = new WeightedPoint();
                    wp.setPoint(new Point(r.value()));
                    wp.setWeight(1);
                    wps.add(wp);
                }
                Input input = new Input();
                input.setDimension(merged.length);
                input.setPoints(wps);
                input.setPermissibleError(0.001);

                WeiszfeldAlgorithm weiszfeld = new WeiszfeldAlgorithm();
                Output output = weiszfeld.process(input);
                Point result = output.getPoint();
                merged = result.getValues();
                break;
            case all:
                merged = new double[behaviour.length * results.size()];
                Iterator<EvaluationResult<double[]>> iter = results.iterator();
                for (int i = 0; i < results.size(); i++) {
                    double[] r = iter.next().value();
                    System.arraycopy(r, 0, merged, i * behaviour.length, behaviour.length);
                }
                break;
        }
        VectorBehaviourResult newVbr = new VectorBehaviourResult(merged);
        newVbr.dist = this.dist;
        newVbr.estimator = this.estimator;
        return newVbr;
    }

    @Override
    public String toString() {
        String res = String.format(Locale.ENGLISH, "%.5f", behaviour[0]);
        for (int i = 1; i < behaviour.length; i++) {
            res += String.format(Locale.ENGLISH, " %.5f", behaviour[i]);
        }
        return res;
    }

    public double vectorDistance(double[] v1, double[] v2) {
        switch (dist) {
            case brayCurtis:
                double diffs = 0;
                double total = 0;
                for (int i = 0; i < v1.length; i++) {
                    diffs += Math.abs(v1[i] - v2[i]);
                    total += v1[i] + v2[i];
                }
                return diffs / total;
            case cosine:
                return cosineSimilarity(v1, v2);
            case manhattan:
                double diff = 0;
                for (int i = 0; i < v1.length; i++) {
                    diff += Math.abs(v1[i] - v2[i]);
                }
                return diff;
            case euclidean:
                double d = 0;
                for (int i = 0; i < v1.length; i++) {
                    d += FastMath.pow2(v1[i] - v2[i]);
                }
                return FastMath.sqrtQuick(d);
        }
        throw new RuntimeException("Not prepared for the given distance: " + dist);
    }

    private double cosineSimilarity(double[] docVector1, double[] docVector2) {
        double dotProduct = 0.0f;
        double magnitude1 = 0.0f;
        double magnitude2 = 0.0f;
        double cosineSimilarity;
        for (int i = 0; i < docVector1.length; i++) {
            dotProduct += docVector1[i] * docVector2[i];  //a.b
            magnitude1 += FastMath.pow2(docVector1[i]);  //(a^2)
            magnitude2 += FastMath.pow2(docVector2[i]); //(b^2)
        }
        magnitude1 = FastMath.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = FastMath.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0f;
        }
        return cosineSimilarity;
    }
}
