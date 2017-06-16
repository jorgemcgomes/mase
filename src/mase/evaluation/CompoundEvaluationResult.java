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
public class CompoundEvaluationResult<T extends EvaluationResult> implements EvaluationResult<List<T>> {

    private static final long serialVersionUID = 1;
    private final ArrayList<T> evals;

    public CompoundEvaluationResult(T... evals) {
        this.evals = new ArrayList<>(Arrays.asList(evals));
    }

    public CompoundEvaluationResult(Collection<T> evals) {
        this.evals = new ArrayList<>(evals);
    }

    public T getEvaluation(int index) {
        return evals.get(index);
    }

    @Override
    public List<T> value() {
        return evals;
    }

    @Override
    public CompoundEvaluationResult<T> mergeEvaluations(Collection<EvaluationResult<List<T>>> results) {
        Collection<T> merged = new ArrayList<>(); 
        // assumes that all the results to merge have the same number of sub-results, so we just check the first
        int n = results.iterator().next().value().size(); 
        for(int i = 0 ; i < n ; i++) {
            ArrayList<T> temp = new ArrayList<>();
            for(EvaluationResult<List<T>> er : results) {
                temp.add(er.value().get(i));
            }
            T m = (T) temp.get(0).mergeEvaluations(temp);
            merged.add(m);
        }
        return new CompoundEvaluationResult<>(merged);
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
