/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.Arrays;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge
 */
public class SemiGenericResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1;
    protected float[] originalResult;

    public SemiGenericResult(float... bs) {
        super(bs);
        this.originalResult = Arrays.copyOf(bs, bs.length);
    }

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

    @Override
    public String toString() {
        String original = super.toString();
        for (int i = 0; i < originalResult.length; i++) {
            original += " " + originalResult[i];
        }
        return original;
    }

}
