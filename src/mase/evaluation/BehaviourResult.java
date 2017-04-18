/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

/**
 *
 * @author jorge
 */
public interface BehaviourResult<T> extends EvaluationResult {

    public double distanceTo(BehaviourResult<T> other);
    
}
