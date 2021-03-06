/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
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
    public static final String P_THRESHOLD = "threshold";

    private FitnessResult res;
    private int accum = 0;
    private double threshold;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.threshold = state.parameters.getDouble(base.push(P_THRESHOLD), null);
    }
        

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        Playground pl = (Playground) sim;
        for(CircularObject o : pl.objects) {
            if(pl.agent.distanceTo(o.getLocation()) < threshold) {
                accum++;
                break;
            }
        }
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        res = new FitnessResult(1 - (double) accum / currentEvaluationStep);
    }
    
    @Override
    public FitnessResult getResult() {
        return res;
    }
    
}
