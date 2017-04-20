/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class ForagingFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult result;

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        ForagingPlayground pl = (ForagingPlayground) sim;
        result = new FitnessResult((double)(pl.objects.size() - pl.itemRemover.aliveObjects.size()) / pl.objects.size());
    }
        
    @Override
    public FitnessResult getResult() {
        return result;
    }
    
}
