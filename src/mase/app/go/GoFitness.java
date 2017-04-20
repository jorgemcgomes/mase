/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author Jorge
 */
public class GoFitness extends MasonEvaluation {

    private CompoundEvaluationResult res;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Go go = (Go) sim;
        int blackScore = go.state.getScore(GoState.BLACK);
        int whiteScore = go.state.getScore(GoState.WHITE);
    
        res = new CompoundEvaluationResult(
                new FitnessResult(100f + blackScore - whiteScore, FitnessResult.ARITHMETIC),  
                new FitnessResult(100f + whiteScore - blackScore, FitnessResult.ARITHMETIC));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
