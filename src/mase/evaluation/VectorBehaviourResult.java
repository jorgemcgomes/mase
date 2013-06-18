/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import mase.EvaluationResult;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class VectorBehaviourResult implements BehaviourResult {

    protected float[] behaviour;
    
    public VectorBehaviourResult(float[] b) {
        this.behaviour = b;
    }
    
    @Override
    public float distanceTo(BehaviourResult other) {
        float res = 0;
        float[] otherB = (float[]) other.value();
        for(int i = 0 ; i < behaviour.length ; i++) {
            res += Math.pow(behaviour[i] - otherB[i], 2);
        }
        return (float) Math.sqrt(res);
    }

    @Override
    public Object value() {
        return behaviour;
    }

    @Override
    public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
        float[] merged = new float[behaviour.length];
        Arrays.fill(merged, 0f);
        for(int i = 0 ; i < merged.length ; i++) {
            for(EvaluationResult r : results) {
                merged[i] += ((float[]) r.value())[i];
            }
            merged[i] /= results.length;
        }
        return new VectorBehaviourResult(merged);
    }

    @Override
    public String toString() {
        String res = "";
        for(int i = 0 ; i < behaviour.length - 1 ; i++) {
            res += behaviour[i] + " ";
        }
        res += behaviour[behaviour.length - 1];
        return res;
    }
    
}
