/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic.systematic;

import java.util.Arrays;
import mase.evaluation.EvaluationResult;
import mase.evaluation.EvaluationResultMerger;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge
 */
public class SystematicResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1;
    protected double[] originalResult;
    private static final SystematicResultMerger SR_MERGER = new SystematicResultMerger();

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
    public String toString() {
        //String original = super.toString();
        String original = "";
        for (int i = 0; i < originalResult.length; i++) {
            original += " " + originalResult[i];
        }
        return original;
    }

    @Override
    public EvaluationResultMerger getResultMerger() {
        return SR_MERGER;
    }
        
    public static class SystematicResultMerger extends VBRMerger {

        @Override
        public EvaluationResult mergeEvaluations(EvaluationResult... evaluations) {
            VectorBehaviourResult vbr = (VectorBehaviourResult)super.mergeEvaluations(evaluations);
            return new SystematicResult(vbr.getBehaviour()); 
        }

        
    }

}
