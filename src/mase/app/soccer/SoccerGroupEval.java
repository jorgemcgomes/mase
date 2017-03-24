/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 * Goals scored | Goals suffered | Game duration | Avg dist of ball to goal | Amount of time with posession of ball
 * @author Jorge
 */
public class SoccerGroupEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private VectorBehaviourResult res;
    private double avgDistBall;
    private double ownPossession;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        avgDistBall = 0;
        ownPossession = 0;
    }    
    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Soccer soc = (Soccer) sim;
               
        // Dist. of the ball to the goal
        avgDistBall += soc.ball.getLocation().distance(soc.rightGoalCenter);
        
        // Own possession
        if(soc.referee.teamPossession == soc.leftTeamColor) {
            ownPossession += 1;
        }
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        Soccer soc = (Soccer) sim;
        res = new VectorBehaviourResult(
                soc.referee.leftTeamScore,
                soc.referee.rightTeamScore,
                currentEvaluationStep / (double) maxEvaluationSteps,
                avgDistBall / currentEvaluationStep / soc.par.fieldLength,
                ownPossession / currentEvaluationStep
        );
    }
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
