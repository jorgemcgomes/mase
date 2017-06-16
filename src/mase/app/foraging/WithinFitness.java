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
public class WithinFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private int time;

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        res = new FitnessResult((double) time / currentEvaluationStep, FitnessResult.MergeMode.arithmetic);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        boolean within = false;
        ForagingTask ft = (ForagingTask) sim;
        Object[] lastRead = ft.flyingBot.botArcs.getClosestObjects();
        for (Object o : lastRead) {
            if (o != null) {
                within = true;
                break;
            }
        }
        if (within) {
            time++;
        }
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        time = 0;
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
