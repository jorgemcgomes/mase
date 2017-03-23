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
    private double initDist;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        MazeTask mt = (MazeTask) sim;
        initDist = mt.target.distanceTo(mt.agent);
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        MazeTask mt = (MazeTask) sim;
        double finalDist = mt.target.distanceTo(mt.agent);
        double norm = (initDist - finalDist) / initDist;
        res = new FitnessResult( Math.min(Math.max(norm, 0),1));
    }
    
    
}
