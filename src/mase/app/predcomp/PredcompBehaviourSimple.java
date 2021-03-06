/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.predcomp;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;
import sim.util.Double2D;

/**
 * Capture time | Average distance to the other | Average distance to walls | Average movement
 * @author jorge
 */
public class PredcompBehaviourSimple extends MasonEvaluation {
    
    protected CompoundEvaluationResult res;
    protected double[] movement;
    protected Double2D[] lastPos;
    protected double distanceToOther;
    protected double maxSteps;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulationProblem.P_MAX_STEPS), null);
    }    
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        this.movement = new double[2];
        this.distanceToOther = 0;
        this.lastPos = new Double2D[2];
    }    

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null); 
        Predcomp pc = (Predcomp) sim;
        distanceToOther += pc.predator.distanceTo(pc.prey);
        
        if(lastPos[0] != null) {
            movement[0] += pc.predator.getLocation().distance(lastPos[0]);
            movement[1] += pc.prey.getLocation().distance(lastPos[1]);
        }      
        
        lastPos[0] = pc.predator.getLocation();
        lastPos[1] = pc.prey.getLocation();
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Predcomp pc = (Predcomp) sim;
        int steps = (int) sim.schedule.getSteps();
        this.res = new CompoundEvaluationResult(
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (distanceToOther / steps / pc.par.size),
                 (movement[0] / steps / pc.par.predatorSpeed)}),
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (distanceToOther / steps / pc.par.size),
                 (movement[1] / steps / pc.par.preySpeed)}));
    }


    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
