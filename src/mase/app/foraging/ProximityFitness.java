/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class ProximityFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private double prox;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        this.prox = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        ForagingTask ft = (ForagingTask) sim;
        double dist = ft.landBot.getLocation().distance(ft.flyingBot.getLocation());
        prox += dist;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        ForagingTask ft = (ForagingTask) sim;
        res = new FitnessResult( (1 - Math.min(1, prox / ft.par.arenaSize.x / currentEvaluationStep)), FitnessResult.MergeMode.arithmetic);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
