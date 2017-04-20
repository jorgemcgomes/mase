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
 *
 * @author jorge
 */
public class PredcompGroupBehaviour extends MasonEvaluation {

    private VectorBehaviourResult res;

    protected double distanceToOther;
    // wall proximity of both?
    // wall movement of both?
    
    /*protected double[] wallProximity;
    protected double[] movement;
    protected Double2D[] lastPos;*/
 
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        //this.wallProximity = new double[2];
        //this.movement = new double[2];
        this.distanceToOther = 0;
        //this.lastPos = new Double2D[2];
    }    

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null); 
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
    
    /*private double prox(Double2D loc) {
        double size = ((Predcomp) sim).par.size;
        double dx = size / 2 - Math.abs(loc.x - size / 2);
        double dy = size / 2 - Math.abs(loc.y - size / 2);
        return  Math.min(dx, dy);
    }*/
        
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Predcomp pc = (Predcomp) sim;
        int steps = (int) sim.schedule.getSteps();
        this.res = new VectorBehaviourResult((double) steps / maxSteps,  (distanceToOther / steps / pc.par.size));
        /*this.res = new SubpopEvaluationResult(
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (wallProximity[0] / steps / (pc.par.size / 2)),
                 (distanceToOther / steps / pc.par.size),
                 (movement[0] / steps / pc.par.predatorSpeed)}),
            new VectorBehaviourResult(new double[]{
                steps / maxSteps, 
                 (wallProximity[1] / steps / (pc.par.size / 2)),
                 (distanceToOther / steps / pc.par.size),
                 (movement[1] / steps / pc.par.preySpeed)}));*/
    }


    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
    
}
