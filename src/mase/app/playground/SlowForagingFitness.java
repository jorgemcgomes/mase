/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.app.playground.ForagingPlayground.ItemRemover.ForagingHook;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;

/**
 *
 * @author jorge
 */
public class SlowForagingFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult result;
    protected int slowForaged;
    public static final String P_SPEED_LIMIT = "speed-limit";
    protected double speedLimit;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        speedLimit = state.parameters.getDouble(base.push(P_SPEED_LIMIT), null);
    }

    @Override
    public FitnessResult getResult() {
        return result;
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        // Determine which items are good to forage and which are not
        ForagingPlayground fp = (ForagingPlayground) sim;
        fp.itemRemover.addForagingHook(new ForagingHook() {
            @Override
            public void foraged(ForagingPlayground sim, PlaygroundAgent ag, CircularObject foraged) {
                if(Math.abs(ag.getSpeed()) < speedLimit) {
                    slowForaged++;
                }
            }
        });
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        // Reward the goods and penalize the bads
        ForagingPlayground pl = (ForagingPlayground) sim;
        result = new FitnessResult(slowForaged / (double) pl.objects.size());
    }
    
}
