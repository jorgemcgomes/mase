/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.Arrays;
import java.util.Locale;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class VectorBehaviourResult implements BehaviourResult {

    private static final long serialVersionUID = 1;
    protected double[] behaviour;
    protected int dist;
    public static final int COSINE = 0, BRAY_CURTIS = 1, EUCLIDEAN = 2, MANHATTAN = 3;


    public VectorBehaviourResult(double... bs) {
        this.behaviour = bs;
        this.dist = EUCLIDEAN;
    }
    
    public void setDistance(int dist) {
        this.dist = dist;
    }

    @Override
    public double distanceTo(BehaviourResult other) {
        return vectorDistance(behaviour, (double[]) other.value());
    }

    @Override
    public Object value() {
        return getBehaviour();
    }

    public void setBehaviour(double[] b) {
        this.behaviour = b;
    }

    public double[] getBehaviour() {
        return this.behaviour;
    }

    @Override
    public VectorBehaviourResult mergeEvaluations(EvaluationResult[] results) {
        double[] merged = new double[behaviour.length];
        Arrays.fill(merged, 0f);
        for (int i = 0; i < merged.length; i++) {
            for (EvaluationResult r : results) {
                merged[i] += ((double[]) r.value())[i];
            }
            merged[i] /= results.length;
        }
        return new VectorBehaviourResult(merged);
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
            case BRAY_CURTIS:
                double diffs = 0;
                double total = 0;
                for (int i = 0; i < v1.length; i++) {
                    diffs += Math.abs(v1[i] - v2[i]);
                    total += v1[i] + v2[i];
                }
                return diffs / total;
            case COSINE:
                return cosineSimilarity(v1, v2);
            case MANHATTAN:
                double diff = 0;
                for(int i = 0 ; i < v1.length ; i++) {
                    diff += Math.abs(v1[i] - v2[i]);
                }
                return diff;
            default:
            case EUCLIDEAN:
                double d = 0;
                for (int i = 0; i < v1.length; i++) {
                    d += FastMath.pow2(v1[i] - v2[i]);
                }
                return FastMath.sqrtQuick(d);
        }
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
