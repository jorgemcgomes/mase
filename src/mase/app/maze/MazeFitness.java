/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.maze;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class MazeFitness extends MasonEvaluation {
    
    private FitnessResult res;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        MazeTask mt = (MazeTask) sim;
        double initDist = mt.par.startPos.distance(mt.par.targetPos) - mt.par.targetRadius - mt.par.agentRadius;
        double finalDist = mt.agent.distanceTo(mt.par.targetPos) - mt.par.targetRadius;
        double norm = (initDist - finalDist) / initDist;
        res = new FitnessResult( Math.min(Math.max(norm, 0),1));
    }
    
    
}
