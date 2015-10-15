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
public class SubpopEvaluationResult implements EvaluationResult {

    private static final long serialVersionUID = 1;
    private ArrayList<EvaluationResult> evals;

    public SubpopEvaluationResult(EvaluationResult... evals) {
        this.evals = new ArrayList<>();
        this.evals.addAll(Arrays.asList(evals));
    }

    public SubpopEvaluationResult(Collection<EvaluationResult> evals) {
        this.evals = new ArrayList<>();
        this.evals.addAll(evals);
    }

    public EvaluationResult getSubpopEvaluation(int index) {
        return evals.get(index);
    }

    public ArrayList<EvaluationResult> getAllEvaluations() {
        return evals;
    }

    @Override
    public Object value() {
        return evals;
    }

    @Override
    public SubpopEvaluationResult mergeEvaluations(EvaluationResult[] results) {
        EvaluationResult[] merged = new EvaluationResult[((SubpopEvaluationResult) results[0]).evals.size()];
        for (int a = 0; a < merged.length; a++) {
            EvaluationResult[] subpopEvals = new EvaluationResult[results.length];
            for (int i = 0; i < results.length; i++) {
                subpopEvals[i] = ((SubpopEvaluationResult) results[i]).evals.get(a);
            }
            merged[a] = subpopEvals[0].mergeEvaluations(subpopEvals);
        }
        return new SubpopEvaluationResult(merged);
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < evals.size(); i++) {
            str += i + ": " + evals.get(i).toString() + "\n";
        }
        return str;
    }

}
