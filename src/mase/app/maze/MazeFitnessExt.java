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
public class MazeFitnessExt extends MasonEvaluation {

    private FitnessResult res;

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        MazeTask mt = (MazeTask) sim;
        double initDist = mt.par.startPos.distance(mt.par.targetPos) - mt.par.targetRadius - mt.par.agentRadius;
        double finalDist = mt.agent.distanceTo(mt.par.targetPos) - mt.par.targetRadius;

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
