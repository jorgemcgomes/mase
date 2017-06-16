/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import java.util.Collection;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import org.apache.commons.math3.util.KthSelector;

/**
 *
 * @author jorge
 */
public class MedianVectorBehaviourResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1L;
    private static final KthSelector SELECTOR = new KthSelector();
    private double[][] raw;

    // vector element / element values
    public MedianVectorBehaviourResult(double[][] rawData) {
        super(medianApply(rawData));
        this.raw = rawData;
    }

    @Override
    public VectorBehaviourResult mergeEvaluations(Collection<EvaluationResult<double[]>> results) {
        int stateLen = ((MedianVectorBehaviourResult) results.iterator().next()).raw.length;
        int totalSteps = 0;
        for (EvaluationResult r : results) {
            totalSteps += ((MedianVectorBehaviourResult) r).raw[0].length;
        }
        double[][] merged = new double[stateLen][totalSteps];
        int index = 0;
        for (EvaluationResult e : results) {
            MedianVectorBehaviourResult m = (MedianVectorBehaviourResult) e;
            if (m.raw.length != stateLen) {
                throw new RuntimeException("Trying to merge two results with different state lengths");
            }
            int steps = m.raw[0].length;
            for (int v = 0; v < stateLen; v++) {
                System.arraycopy(m.raw[v], 0, merged[v], index, steps);
            }
            index += steps;
        }
        MedianVectorBehaviourResult newM = new MedianVectorBehaviourResult(merged);
        // once merged, delete raw data to avoid huge memory footprint
        // note that this prevents further merges
        newM.raw = null;
        return newM;
        
    }


    private static double[] medianApply(double[][] raw) {
        double[] res = new double[raw.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = median(raw[i]);
        }
        return res;
    }

    private static double median(double[] values) {
        return SELECTOR.select(values, null, values.length / 2);
    }
}
