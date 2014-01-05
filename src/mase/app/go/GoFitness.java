/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author Jorge
 */
public class GoFitness extends MasonEvaluation {

    private SubpopEvaluationResult res;
    
    @Override
    protected void postSimulation() {
        Go go = (Go) sim;
        int blackScore = go.state.getScore(GoState.BLACK);
        int whiteScore = go.state.getScore(GoState.WHITE);
    
        res = new SubpopEvaluationResult(
                new FitnessResult(1000f + blackScore - whiteScore),  
                new FitnessResult(1000f + whiteScore - blackScore));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
