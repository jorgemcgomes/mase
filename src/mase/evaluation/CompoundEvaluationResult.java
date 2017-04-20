/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class CompoundEvaluationResult implements EvaluationResult<ArrayList<? extends EvaluationResult>> {

    private static final long serialVersionUID = 1;
    private final ArrayList<? extends EvaluationResult> evals;

    public CompoundEvaluationResult(EvaluationResult... evals) {
        this.evals = new ArrayList<>(Arrays.asList(evals));
    }

    public CompoundEvaluationResult(Collection<? extends EvaluationResult> evals) {
        this.evals = new ArrayList<>(evals);
    }

    public EvaluationResult getEvaluation(int index) {
        return evals.get(index);
    }

    @Override
    public ArrayList<? extends EvaluationResult> value() {
        return evals;
    }

    @Override
    public CompoundEvaluationResult mergeEvaluations(EvaluationResult[] results) {
        EvaluationResult[] merged = new EvaluationResult[((CompoundEvaluationResult) results[0]).evals.size()];
        for (int a = 0; a < merged.length; a++) {
            EvaluationResult[] subpopEvals = new EvaluationResult[results.length];
            for (int i = 0; i < results.length; i++) {
                subpopEvals[i] = ((CompoundEvaluationResult) results[i]).evals.get(a);
            }
            merged[a] = subpopEvals[0].mergeEvaluations(subpopEvals);
        }
        return new CompoundEvaluationResult(merged);
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < evals.size(); i++) {
            str += i + " " + evals.get(i).toString() + " ";
        }
        return str.trim();
    }

}
