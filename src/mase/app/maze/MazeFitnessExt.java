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
public class MazeFitnessExt extends MasonEvaluation {

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
        initDist = mt.agent.distanceTo(mt.target);
    }    
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        MazeTask mt = (MazeTask) sim;
        double finalDist = mt.agent.distanceTo(mt.target);

        // Objective reached -- add time
        if (finalDist <= 0.001) {
            double t = (super.maxSteps - mt.schedule.getSteps()) / (double) maxSteps;
            res = new FitnessResult(1 + t);
        } else {
            double norm = (initDist - finalDist) / initDist;
            res = new FitnessResult( Math.min(Math.max(norm, 0), 1));
        }
    }

}
