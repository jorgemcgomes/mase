/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 * Goals scored | Goals suffered | Avg. dist. of the ball to the goal | 
 * Avg min dist. of team to ball (own possesion) | Avg min dist of opps to ball (opps' possession)
 * @author Jorge
 */
public class SoccerGroupEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private VectorBehaviourResult res;
    private double avgDistBall;
    private double ownPossession;
    private double oppPossession;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        avgDistBall = 0;
        ownPossession = 0;
        oppPossession = 0;
    }    
    
    @Override
    protected void evaluate() {
        super.evaluate();
        Soccer soc = (Soccer) sim;
        
        // Dist. of the ball to the goal
        avgDistBall += soc.ball.getLocation().distance(soc.rightGoalCenter);
        
        // Min dist. of team to ball (own possesion)
        double min = Double.POSITIVE_INFINITY;
        for(SoccerAgent sa : soc.leftTeam) {
            min = Math.min(min, sa.distanceTo(soc.ball));
        }
        ownPossession += min;
        
        // Min dist. of team to ball (own possesion)
        min = Double.POSITIVE_INFINITY;
        for(SoccerAgent sa : soc.rightTeam) {
            min = Math.min(min, sa.distanceTo(soc.ball));
        }       
        oppPossession += min;
    }
    
    @Override
    protected void postSimulation() {
        super.postSimulation();
        Soccer soc = (Soccer) sim;
        res = new VectorBehaviourResult(
                soc.referee.leftTeamScore,
                soc.referee.rightTeamScore,
                avgDistBall / currentEvaluationStep / soc.par.fieldLength,
                ownPossession / currentEvaluationStep / soc.par.fieldLength,
                oppPossession / currentEvaluationStep / soc.par.fieldLength
        );
    }
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
