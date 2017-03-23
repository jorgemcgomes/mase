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
public class WinsFitnessSimple extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private double minDist = Double.POSITIVE_INFINITY;

    private FitnessResult res;
    
    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Soccer soc = (Soccer) sim;
        minDist = Math.min(minDist, soc.ball.getCenterLocation().distance(soc.rightGoalCenter));
    }
    
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null); 
        Soccer soc = (Soccer) sim;
        double bootstrap = 1 - minDist / FastMath.sqrt(FastMath.pow2(soc.par.fieldLength) + FastMath.pow2(soc.par.fieldWidth / 2));
        res = new FitnessResult(soc.referee.leftTeamScore > 0 ? 1 : bootstrap / 100, FitnessResult.ARITHMETIC);
    }
    
    
}

