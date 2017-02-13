/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic.systematic;

import java.util.Arrays;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge
 */
public class SystematicResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1;
    protected double[] originalResult;

    public SystematicResult(double... bs) {
        super(bs);
        this.originalResult = Arrays.copyOf(bs, bs.length);
    }

    public double[] getOriginalResult() {
        return originalResult;
    }

    public void setOriginalResult(double[] originalResult) {
        this.originalResult = originalResult;
    }

    @Override
    public VectorBehaviourResult mergeEvaluations(EvaluationResult[] results) {
        VectorBehaviourResult vbr = super.mergeEvaluations(results);
        return new SystematicResult(vbr.getBehaviour());
    }

    @Override
    public String toString() {
        //String original = super.toString();
        String original = "";
        for (int i = 0; i < originalResult.length; i++) {
            original += " " + originalResult[i];
        }
        return original;
    }

}
