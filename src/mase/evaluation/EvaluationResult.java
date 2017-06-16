/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author jorge
 */
public interface EvaluationResult<T> extends Serializable {
    
    public T value();
    
    /**
     * Merges the given EvaluationResults -- it should not use nor modify the attributes of this object
     * @param results To be merged
     * @return A new EvaluationResult with the merged results 
     */
    public EvaluationResult<T> mergeEvaluations(Collection<EvaluationResult<T>> results);
    
}
