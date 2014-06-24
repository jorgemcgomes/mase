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
public class HerdingFitness2 extends MasonEvaluation {

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
        // sheep lost
        /*if (herd.sheep.status == Sheep.Status.DEAD || herd.sheep.status == Sheep.Status.ESCAPED) {
            fitness = herd.schedule.getSteps() / (double) maxSteps;
        } else if (herd.sheep.status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 3 - herd.schedule.getSteps() / (double) maxSteps;
        } else { // sheep still in play
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
            fitness = 2 - herd.sheep.getLocation().distance(gate) / maxDist;
        }*/

        /*if (herd.sheep.status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 3 - herd.schedule.getSteps() / (double) maxSteps;
        } else {
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
            fitness = 1 - herd.sheep.getLocation().distance(gate) / maxDist + herd.schedule.getSteps() / (double) maxSteps;
        }*/
        
        if (herd.sheeps.get(0).status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 2 - herd.schedule.getSteps() / (double) maxSteps;
        } else {
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
            fitness = 1 - herd.sheeps.get(0).getLocation().distance(gate) / maxDist;
        }      

        /*if (herd.sheep.status == Sheep.Status.DEAD) {
            fitness = herd.schedule.getSteps() / (double) maxSteps;
        } else if (herd.sheep.status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 3 - herd.schedule.getSteps() / (double) maxSteps;
        } else { // sheep still in play
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
            fitness = 2 - herd.sheep.getLocation().distance(gate) / maxDist;
        }*/      
        
        res = new FitnessResult((float) fitness, FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
