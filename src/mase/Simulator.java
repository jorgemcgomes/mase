package mase;

import ec.Singleton;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
public interface Simulator extends Singleton {

    public EvaluationResult[] evaluateSolution(GroupController gc, long seed);
    
}
