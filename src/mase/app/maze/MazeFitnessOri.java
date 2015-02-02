/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.maze;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class MazeFitnessOri extends MasonEvaluation {
    
    private FitnessResult res;
    public static final double NORM = 1500;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        MazeTask mt = (MazeTask) sim;
        
        
        double finalDist = Math.max(0, mt.agent.distanceTo(mt.par.targetPos) - mt.par.targetRadius);
        
        res = new FitnessResult((float) (1 - finalDist / NORM));
    }
    
    
}
