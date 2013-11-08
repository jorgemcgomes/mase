/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.indiana;

import mase.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class IndianaFitness extends MasonEvaluation {
    
    private FitnessResult res;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation() {
        Indiana ind = (Indiana) sim;
        int count = 0;
        for(IndianaAgent a : ind.agents) {
            if(a.escaped) {
                count++;
            }
        }
        res = new FitnessResult((float) count / ind.agents.size());
    }
    
    
    
}
