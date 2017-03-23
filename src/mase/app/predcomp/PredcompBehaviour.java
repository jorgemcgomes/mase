/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.predcomp;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;
import sim.util.Double2D;

/**
 * Capture time | Average distance to the other | Average distance to walls | Average movement
 * @author jorge
 */
public class PredcompBehaviour extends MasonEvaluation {
    
    protected SubpopEvaluationResult res;
    protected double[] wallProximity;
    protected double[] movement;
    protected double distanceToOther;
    protected Double2D[] lastPos;
    protected double maxSteps;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulationProblem.P_MAX_STEPS), null);
    }    
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        this.wallProximity = new double[2];
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
            movement[0] += pc.predator.getCenterLocation().distance(lastPos[0]);
            movement[1] += pc.prey.getCenterLocation().distance(lastPos[1]);
        }      
        
        lastPos[0] = pc.predator.getCenterLocation();
        lastPos[1] = pc.prey.getCenterLocation();
        
        wallProximity[0] += prox(pc.predator.getCenterLocation(), pc.par.size);
        wallProximity[1] += prox(pc.prey.getCenterLocation(), pc.par.size);
    }
    
    private double prox(Double2D loc, double size) {
        double dx = size / 2 - Math.abs(loc.x - size / 2);
        double dy = size / 2 - Math.abs(loc.y - size / 2);
        return  Math.min(dx, dy);
    }
        
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Predcomp pc = (Predcomp) sim;
        int steps = (int) sim.schedule.getSteps();
        this.res = new SubpopEvaluationResult(
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (wallProximity[0] / steps / (pc.par.size / 2)),
                 (distanceToOther / steps / pc.par.size),
                 (movement[0] / steps / pc.par.predatorSpeed)}),
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (wallProximity[1] / steps / (pc.par.size / 2)),
                 (distanceToOther / steps / pc.par.size),
                 (movement[1] / steps / pc.par.preySpeed)}));
    }


    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
