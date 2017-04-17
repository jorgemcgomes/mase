/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.Prototype;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public interface EvaluationFunction<T extends EvaluationResult> extends Prototype {
    
    public T getResult();
        
}
