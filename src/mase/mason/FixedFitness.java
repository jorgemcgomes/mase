/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.evaluation.FitnessResult;

/**
 * Returns a random fitness score. Useful for testing and debugging purposes.
 * @author jorge
 */
public class FixedFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult fr;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        fr = new FitnessResult(0);
    }
    
    @Override
    public FitnessResult getResult() {
        return fr;
    }
    
}
