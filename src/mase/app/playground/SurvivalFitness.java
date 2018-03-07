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
public class SurvivalFitness  extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;
    private FitnessResult r;

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        Playground pl = (Playground) sim;
        for(CircularObject o : pl.objects) {
            if(pl.agent.distanceTo(o) == 0) {
                pl.kill();
            }
        }
    }
        
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        Playground pl = (Playground) sim;
        r = new FitnessResult(pl.schedule.getTime() / maxSteps);
    }

    @Override
    public FitnessResult getResult() {
        return r;
    }
}
