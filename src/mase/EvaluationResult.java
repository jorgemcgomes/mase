/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public interface EvaluationResult extends Serializable {
    
    public Object value();
    
    public EvaluationResult mergeEvaluations(EvaluationResult[] results);
    
}
