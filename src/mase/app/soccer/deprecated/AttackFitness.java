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
public class AttackFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private double minDist = Double.POSITIVE_INFINITY;
    
    private FitnessResult res;
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        Soccer soc = (Soccer) sim;
        minDist = Math.min(minDist, soc.ball.getLocation().distance(soc.rightGoalCenter));
    }
    
    

    @Override
    protected void postSimulation() {
        super.postSimulation(); 
        Soccer soc = (Soccer) sim;
        res = new FitnessResult(soc.referee.leftTeamScore < 1 ? 
                1 - minDist / FastMath.sqrt(FastMath.pow2(soc.par.fieldLength) + FastMath.pow2(soc.par.fieldWidth / 2)) :
                soc.referee.leftTeamScore, 
                FitnessResult.MergeMode.arithmetic);
    }
    
    
}
