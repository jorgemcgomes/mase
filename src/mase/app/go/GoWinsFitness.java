/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.go;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class GoWinsFitness extends MasonEvaluation {
    
        private SubpopEvaluationResult res;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Go go = (Go) sim;
        int blackScore = go.state.getScore(GoState.BLACK);
        int whiteScore = go.state.getScore(GoState.WHITE);
        
        double blackFit, whiteFit;
        if(blackScore > whiteScore) {
            blackFit = 1;
            whiteFit = 0;
        } else if(blackScore < whiteScore) {
            blackFit = 0;
            whiteFit = 1;
        } else {
            blackFit = 0.5f;
            whiteFit = 0.5f;
        }
    
        res = new SubpopEvaluationResult(
                new FitnessResult(blackFit, FitnessResult.ARITHMETIC),  
                new FitnessResult(whiteFit, FitnessResult.ARITHMETIC));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
