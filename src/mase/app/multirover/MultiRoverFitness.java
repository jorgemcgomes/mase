/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class MultiRoverFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    protected FitnessResult fitnessResult;
    
    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        MultiRover mr = (MultiRover) sim;
        int count = 0;
        for(int c : mr.scores) {
            count += c;
        }
        fitnessResult = new FitnessResult(count);
    }
    
    
    
    
}
