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
public class HeightFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private double height;

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        ForagingTask ft = (ForagingTask) sim;
        height += Math.abs(ft.flyingBot.effector.getHeight() - (ft.flyingBot.effector.getMaxHeight() - 10));
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        height = 0;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        ForagingTask ft = (ForagingTask) sim;
        res = new FitnessResult( (1 - Math.min(1, height / ft.flyingBot.effector.getMaxHeight() / currentEvaluationStep)), FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
