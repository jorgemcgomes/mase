/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jorge
 */
public class MultiVectorBehaviourResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1L;
    private final Collection<EvaluationResult<double[]>> samples;

    public MultiVectorBehaviourResult(Collection<EvaluationResult<double[]>> samples) {
        super();
        this.samples = samples;
        double[] merged = new double[behaviour.length * samples.size()];
        Iterator<EvaluationResult<double[]>> iter = samples.iterator();
        for (int i = 0; i < samples.size(); i++) {
            double[] r = iter.next().value();
            System.arraycopy(r, 0, merged, i * behaviour.length, behaviour.length);
        }
        this.behaviour = merged;
    }

    @Override
    public double distanceTo(BehaviourResult other) {
        MultiVectorBehaviourResult o = (MultiVectorBehaviourResult) other;
        double totalDistance = 0;
        Iterator<EvaluationResult<double[]>> iterThis = this.samples.iterator();
        Iterator<EvaluationResult<double[]>> iterOther = o.samples.iterator();
        int count = 0;
        while (iterThis.hasNext() && iterOther.hasNext()) {
            totalDistance += ((BehaviourResult) iterThis.next()).distanceTo((BehaviourResult) iterOther.next());
            count++;
        }
        if (iterThis.hasNext() || iterOther.hasNext()) {
            throw new RuntimeException("Trying to merge two MultiVectorBehaviourResult with different number of samples.");
        }
        return totalDistance / count;
    }
    
    public Collection<EvaluationResult<double[]>> getSamples() {
        return samples;
    }
}
