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
import mase.mason.MasonSimulator;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PredcompGroupBehaviour extends MasonEvaluation {

    private VectorBehaviourResult res;

    protected float distanceToOther;
    protected float maxSteps;
    // wall proximity of both?
    // wall movement of both?
    
    /*protected float[] wallProximity;
    protected float[] movement;
    protected Double2D[] lastPos;*/
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
    }    
    
    @Override
    protected void preSimulation() {
        super.preSimulation();
        //this.wallProximity = new float[2];
        //this.movement = new float[2];
        this.distanceToOther = 0;
        //this.lastPos = new Double2D[2];
    }    

    @Override
    protected void evaluate() {
        super.evaluate(); 
        Predcomp pc = (Predcomp) sim;
        distanceToOther += pc.predator.distanceTo(pc.prey);
        
        /*if(lastPos[0] != null) {
            movement[0] += pc.predator.getLocation().distance(lastPos[0]);
            movement[1] += pc.prey.getLocation().distance(lastPos[1]);
        }      
        
        lastPos[0] = pc.predator.getLocation();
        lastPos[1] = pc.prey.getLocation();
        
        wallProximity[0] += prox(pc.predator.getLocation());
        wallProximity[1] += prox(pc.prey.getLocation());*/
    }
    
    /*private float prox(Double2D loc) {
        double size = ((Predcomp) sim).par.size;
        double dx = size / 2 - Math.abs(loc.x - size / 2);
        double dy = size / 2 - Math.abs(loc.y - size / 2);
        return (float) Math.min(dx, dy);
    }*/
        
    
    @Override
    protected void postSimulation() {
        Predcomp pc = (Predcomp) sim;
        int steps = (int) sim.schedule.getSteps();
        this.res = new VectorBehaviourResult(steps / maxSteps, (float) (distanceToOther / steps / pc.par.size));
        /*this.res = new SubpopEvaluationResult(
            new VectorBehaviourResult(new float[]{
                steps / maxSteps, 
                (float) (wallProximity[0] / steps / (pc.par.size / 2)),
                (float) (distanceToOther / steps / pc.par.size),
                (float) (movement[0] / steps / pc.par.predatorSpeed)}),
            new VectorBehaviourResult(new float[]{
                steps / maxSteps, 
                (float) (wallProximity[1] / steps / (pc.par.size / 2)),
                (float) (distanceToOther / steps / pc.par.size),
                (float) (movement[1] / steps / pc.par.preySpeed)}));*/
    }


    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
    
}
