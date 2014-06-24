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
public class HerdingFitness4 extends MasonEvaluation {

    private FitnessResult res;
    private double sheepSafety;

    @Override
    protected void preSimulation() {
        super.preSimulation();
    }

    @Override
    protected void evaluate() {
        super.evaluate(); 
        Herding herd = (Herding) super.sim;
        double minDist = Double.POSITIVE_INFINITY;
        for(Fox f : herd.foxes) {
            minDist = Math.min(minDist, f.distanceTo(herd.sheeps.get(0)));
        }
        sheepSafety += minDist;
    }


    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) super.sim;

        Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
        double fitness = 1 - herd.sheeps.get(0).getLocation().distance(gate) / maxDist + sheepSafety / maxDist / currentEvaluationStep;
        
        /*if (herd.sheeps.get(0).status == Sheep.Status.CAPTURED) { // sheep curraled
         fitness = 2 - herd.schedule.getSteps() / (double) maxSteps;
         } else {

         fitness = 1 - herd.sheeps.get(0).getLocation().distance(gate) / maxDist;
         // fitness = 1 - distance + distance
         }    */
        res = new FitnessResult((float) fitness, FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
