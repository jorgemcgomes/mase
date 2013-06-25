/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Singleton;

/**
 *
 * @author jorge
 */
public interface PostEvaluator extends Singleton {

    public void processPopulation(EvolutionState state);
    
}
