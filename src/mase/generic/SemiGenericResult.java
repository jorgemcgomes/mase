/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.Arrays;
import mase.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge
 */
public class SemiGenericResult extends VectorBehaviourResult {

    public SemiGenericResult(float... bs) {
        super(bs);
        this.originalResult = Arrays.copyOf(bs, bs.length);
    }

    protected float[] originalResult;

    public float[] getOriginalResult() {
        return originalResult;
    }

    public void setOriginalResult(float[] originalResult) {
        this.originalResult = originalResult;
    }

    @Override
    public VectorBehaviourResult mergeEvaluations(EvaluationResult[] results) {
        VectorBehaviourResult vbr = super.mergeEvaluations(results);
        return new SemiGenericResult(vbr.getBehaviour());
    }

}
