/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class ForagingFitness extends MasonEvaluation {
    
        private FitnessResult res;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        ForagingTask ft = (ForagingTask) sim;
        res = new FitnessResult( (ft.par.items.length - ft.items.size()), FitnessResult.ARITHMETIC);
    }
    
    
}
