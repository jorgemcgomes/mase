/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class TournamentFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
    @Override
    protected void postSimulation() {
        super.postSimulation(); 
        Soccer soc = (Soccer) sim;
        double score;
        if(soc.referee.leftTeamScore > 0) { // win
            score = 3;
        } else if(soc.referee.rightTeamScore == 0) { // tie
            score = 1;
        } else { // loose
            score = 0;
        }
        res = new FitnessResult(score, FitnessResult.ARITHMETIC);
    }
    
    
}

