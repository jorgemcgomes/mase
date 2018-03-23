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
public class WallAggregationFitness extends AggregationFitness {
    
    private static final long serialVersionUID = 1L;
    protected double wallDistAccum;
    public static final String P_NEAR_WALLS = "near-walls";
    private boolean nearWalls; // reward aggregating near walls or away from walls? (true/false respectively)

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        nearWalls = state.parameters.getBoolean(base.push(P_NEAR_WALLS), null, true);
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        wallDistAccum = 0;
    }
    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        double wallDist =  Math.min(Math.min(currentCM.x, currentCM.y), 
                Math.min(sw.par.arenaSize - currentCM.x, sw.par.arenaSize - currentCM.y));
        wallDistAccum += wallDist;
    }

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        double aggFitness = super.getFinalTaskFitness(sim); 
        double accumNorm = wallDistAccum / (sim.par.arenaSize / 2) / currentEvaluationStep;
        double wallCoefficient = nearWalls ? 1 - accumNorm : accumNorm;
        return aggFitness * wallCoefficient;
    }
    
    
    
}
