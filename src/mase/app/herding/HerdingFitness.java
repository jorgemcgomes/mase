/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import org.apache.commons.math3.util.FastMath;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class HerdingFitness extends MasonEvaluation {

    private FitnessResult res;

    @Override
    protected void preSimulation() {
        super.preSimulation();
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) super.sim;
        double fitness;
        
        if (herd.sheeps.get(0).status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 2 - herd.schedule.getSteps() / (double) maxSteps;
        } else {
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
            fitness = 1 - herd.sheeps.get(0).getLocation().distance(gate) / maxDist;
        }      
        res = new FitnessResult((float) fitness, FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
