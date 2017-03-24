/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class TournamentFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private double avgDist;
    private FitnessResult res;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        this.avgDist = 0;
    }
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Soccer soc = (Soccer) sim;
        avgDist += soc.ball.getLocation().distance(soc.rightGoalCenter);
    }
        
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null); 
        Soccer soc = (Soccer) sim;
        double score;
        if(soc.referee.leftTeamScore > 0) { // win
            score = 3;
        } else if(soc.referee.rightTeamScore == 0) { // tie
            score = 1;
        } else { // loose
            score = 0;
        }
        double bootstrap = 1 - avgDist / currentEvaluationStep / FastMath.sqrt(FastMath.pow2(soc.par.fieldLength) + FastMath.pow2(soc.par.fieldWidth / 2));
        res = new FitnessResult(score + bootstrap, FitnessResult.ARITHMETIC);
    }
    
    
}

