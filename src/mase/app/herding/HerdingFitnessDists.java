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
public class HerdingFitnessDists extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private double accum;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        this.accum = 0;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        Herding herd = (Herding) super.sim;
        Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        accum += gate.distance(herd.sheeps.get(0).getLocation());
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) super.sim;
        if (currentEvaluationStep < maxEvaluationSteps) {
            Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
            accum += gate.distance(herd.sheeps.get(0).getLocation()) * (maxEvaluationSteps - currentEvaluationStep);
        }
        double maxDist = FastMath.sqrt(FastMath.pow(herd.par.arenaSize, 2) + FastMath.pow(herd.par.arenaSize / 2, 2));
        res = new FitnessResult( (1 - accum / maxEvaluationSteps / maxDist), FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
