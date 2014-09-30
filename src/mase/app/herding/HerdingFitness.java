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
    private double initialDistance;
    private Double2D gate;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Herding herd = (Herding) super.sim;
        gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        initialDistance = herd.sheeps.get(0).distanceTo(gate);
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) super.sim;
        double fitness;
        
        if (herd.sheeps.get(0).status == Sheep.Status.CAPTURED) { // sheep curraled
            fitness = 2 - herd.schedule.getSteps() / (double) maxSteps;
        } else {
            fitness = Math.max(0, 1 - herd.sheeps.get(0).distanceTo(gate) / initialDistance);
        }      
        res = new FitnessResult((float) fitness, FitnessResult.HARMONIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
