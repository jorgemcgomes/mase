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
public class HerdingFitness3 extends MasonEvaluation {

    private FitnessResult res;

    @Override
    protected void preSimulation() {
        super.preSimulation();
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) super.sim;

        double fitness = 0;

        Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
        for (Sheep s : herd.sheeps) {
            fitness += 1 - s.getLocation().distance(gate) / maxDist;
        }
        res = new FitnessResult((float) fitness, FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
