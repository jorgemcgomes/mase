/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class FlockingFitness extends SwarmFitness {
    
    public static final String P_FLOCK_DISTANCE = "flock-dist";
    private static final long serialVersionUID = 1L;
    protected double flockDist;
    protected double sum;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.flockDist = state.parameters.getDouble(base.push(P_FLOCK_DISTANCE), null);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim); 
        sum = 0;
    }

    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        double f = 0;
        int count = 0;
        for(int i = 0 ; i < sw.agents.size() ; i++) {
            for(int j = i+1 ; j < sw.agents.size() ; j++) {
                count++;
                SwarmAgent si = sw.agents.get(i);
                SwarmAgent sj = sw.agents.get(j);
                if(si.distanceTo(sj) < flockDist) {
                    double ai = si.orientation2D() + Math.PI; // [0,2PI]
                    double aj = sj.orientation2D() + Math.PI; // [0,2PI]
                    double angleDifference = Math.min((2 * Math.PI) - Math.abs(ai - aj), Math.abs(ai - aj)); // [0,PI]
                    double vi = si.getSpeed() / sw.par.wheelSpeed; // [-1,1]
                    double vj = sj.getSpeed() / sw.par.wheelSpeed; // [-1,1]
                    // v1 and v2 need the same sign
                    // angle differences greater than pi/2 do not count
                    f += (1 - Math.min(1, angleDifference * 2 / Math.PI)) * Math.max(0, vi * vj); 
                }                
            }
        }
        sum += f / count;
    }    
        

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        // The fitness value intuitively means:
        // how well aligned (between 0 and 1) was on average each agent with all its neighbours
        return sum / currentEvaluationStep;
    }   
}
