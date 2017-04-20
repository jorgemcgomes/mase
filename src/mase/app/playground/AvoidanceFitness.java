/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;

/**
 *
 * @author jorge
 */
public class AvoidanceFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private double accum = 0;

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        Playground pl = (Playground) sim;
        for(CircularObject o : pl.objects) {
            accum += pl.agent.distanceTo(o.getLocation());
        }
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        Playground pl = (Playground) sim;
        res = new FitnessResult(accum / (double) currentEvaluationStep/ pl.objects.size() / pl.par.arenaSize);
    }
    
    @Override
    public FitnessResult getResult() {
        return res;
    }
    
}
