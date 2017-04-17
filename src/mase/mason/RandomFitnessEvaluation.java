/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;

/**
 * Returns a random fitness score. Useful for testing and debugging purposes.
 * @author jorge
 */
public class RandomFitnessEvaluation extends MasonEvaluation {

    private FitnessResult fr;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        fr = new FitnessResult(sim.random.nextDouble());
    }

    
    
    @Override
    public EvaluationResult getResult() {
        return fr;
    }
    
}
