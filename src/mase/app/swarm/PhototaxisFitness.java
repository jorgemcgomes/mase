/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import mase.mason.MasonSimState;

/**
 *
 * @author Jorge
 */
public class PhototaxisFitness extends SwarmFitness {
    
    private static final long serialVersionUID = 1L;
    private double sum = 0;
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim); 
        this.sum = 0;
    }
    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        if(sw.objects.size() != 1) {
            throw new RuntimeException("Was expecting exactly one object/POI, got: " + sw.objects.size());
        }
        POI obj = sw.objects.get(0);
        double d = 0;
        for(SwarmAgent sa: sw.agents) {
            d += sa.distanceTo(obj);
        }
        sum += d / sw.agents.size();
    }
    
    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        return 1 - sum / currentEvaluationStep / sim.par.arenaSize;
    }

    
    
}
