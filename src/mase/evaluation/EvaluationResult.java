/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public interface EvaluationResult<T> extends Serializable {

    public T value();

    /**
     * The returned EvaluationResultMerger should be capable of merging
     * EvaluationResult's of the same type of this one
     *
     * @return
     */
    public EvaluationResultMerger getResultMerger();

}
