/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SubpopEvaluationResult implements EvaluationResult<List<EvaluationResult>> {

    private static final long serialVersionUID = 1;
    private final ArrayList<EvaluationResult> evals;
    private static final MetaMerger MERGER = new MetaMerger();

    public SubpopEvaluationResult(EvaluationResult... evals) {
        this.evals = new ArrayList<>(Arrays.asList(evals));
    }

    public SubpopEvaluationResult(Collection<EvaluationResult> evals) {
        this.evals = new ArrayList<>(evals);
    }

    public EvaluationResult getSubpopEvaluation(int index) {
        return evals.get(index);
    }

    public ArrayList<EvaluationResult> getAllEvaluations() {
        return evals;
    }

    @Override
    public List<EvaluationResult> value() {
        return evals;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < evals.size(); i++) {
            str += i + " " + evals.get(i).toString() + " ";
        }
        return str.trim();
    }

    @Override
    public EvaluationResultMerger getResultMerger() {
        return MERGER;
    }
    
    public static class MetaMerger implements EvaluationResultMerger {

        @Override
        public EvaluationResult mergeEvaluations(EvaluationResult[] evaluations) {
            EvaluationResult[] merged = new EvaluationResult[((SubpopEvaluationResult) evaluations[0]).evals.size()];
            for (int a = 0; a < merged.length; a++) {
                EvaluationResult[] subpopEvals = new EvaluationResult[evaluations.length];
                for (int i = 0; i < evaluations.length; i++) {
                    subpopEvals[i] = ((SubpopEvaluationResult) evaluations[i]).evals.get(a);
                }
                merged[a] = subpopEvals[0].getResultMerger().mergeEvaluations(subpopEvals);
            }
            return new SubpopEvaluationResult(merged);            
        }
        
    }
}
