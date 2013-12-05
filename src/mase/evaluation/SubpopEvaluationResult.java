/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SubpopEvaluationResult implements EvaluationResult {

    private final EvaluationResult[] evals;

    public SubpopEvaluationResult(EvaluationResult[] evals) {
        this.evals = evals;
    }

    public EvaluationResult getSubpopEvaluation(int index) {
        return evals[index];
    }
    
    public EvaluationResult[] getAllEvaluations() {
        return evals;
    }

    @Override
    public Object value() {
        return evals;
    }

    @Override
    public SubpopEvaluationResult mergeEvaluations(EvaluationResult[] results) {
        EvaluationResult[] merged = new EvaluationResult[((SubpopEvaluationResult) results[0]).evals.length];
        for (int a = 0; a < merged.length; a++) {
            EvaluationResult[] subpopEvals = new EvaluationResult[results.length];
            for (int i = 0; i < results.length; i++) {
                subpopEvals[i] = ((SubpopEvaluationResult) results[i]).evals[a];
            }
            merged[a] = subpopEvals[0].mergeEvaluations(subpopEvals);
        }
        return new SubpopEvaluationResult(merged);
    }

    @Override
    public String toString() {
        String str = "";
        for(int i = 0 ; i < evals.length ; i++) {
            str += i+": " + evals[i].toString() + "\n";
        }
        return str;
    }
    
    
}
