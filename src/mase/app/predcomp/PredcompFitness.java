/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.predcomp;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class PredcompFitness extends MasonEvaluation {

    private SubpopEvaluationResult ser;

    @Override
    protected void postSimulation() {
        ser = new SubpopEvaluationResult(
                new FitnessResult(1000f - sim.schedule.getSteps(), FitnessResult.ARITHMETIC),
                new FitnessResult((float) sim.schedule.getSteps(), FitnessResult.ARITHMETIC)
        );
    }
    
    @Override
    public EvaluationResult getResult() {
        return ser;
    }
}
