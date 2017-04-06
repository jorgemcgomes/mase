/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.metest;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class DistanceFitness extends MasonEvaluation {

    private double accumDistance;
    private Double2D lastPos;
    private FitnessResult fr;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        LocomotionTask lt = (LocomotionTask) sim;
        lastPos = lt.agent.getLocation();
        accumDistance = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        LocomotionTask lt = (LocomotionTask) sim;
        accumDistance += lt.agent.getLocation().distance(lastPos);
        lastPos = lt.agent.getLocation();
    }
        
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        this.fr = new FitnessResult(1 - accumDistance / currentEvaluationStep);
    }
    
    
    @Override
    public EvaluationResult getResult() {
        return fr;
    }
    
}
