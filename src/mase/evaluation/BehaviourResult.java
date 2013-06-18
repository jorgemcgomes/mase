/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import mase.EvaluationResult;

/**
 *
 * @author jorge
 */
public interface BehaviourResult extends EvaluationResult {

    public float distanceTo(BehaviourResult other);
    
}
