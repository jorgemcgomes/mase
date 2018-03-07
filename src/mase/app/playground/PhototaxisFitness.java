/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class PhototaxisFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult r;

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        Playground pl = (Playground) sim;
        if(pl.agent.distanceTo(pl.objects.get(0).getLocation()) <= 1) {
            pl.kill();
        }
    }
        
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        Playground pl = (Playground) sim;
        double finalDist = pl.agent.distanceTo(pl.objects.get(0).getLocation());
        double diag = FastMath.sqrt(FastMath.pow2(pl.field.width) + FastMath.pow2(pl.field.height));
        r = new FitnessResult((finalDist > 1 ? 1 - finalDist / diag : 2 - (double) pl.schedule.getTime() / maxSteps) / 2);
    }

    @Override
    public FitnessResult getResult() {
        return r;
    }
    
}
