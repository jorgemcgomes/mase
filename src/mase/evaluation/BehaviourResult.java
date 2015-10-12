/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

/**
 *
 * @author jorge
 */
public interface BehaviourResult extends EvaluationResult {

    public double distanceTo(BehaviourResult other);
    
}
