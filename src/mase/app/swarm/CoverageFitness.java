/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 * @author jorge
 */
public class CoverageFitness extends SwarmFitness {

    protected double cellSize;
    protected double[][] visited;
    protected double[][] accum;
    protected double decay;
    
    public static final String P_CELL_SIZE = "cell-size";
    public static final String P_DECAY = "decay-steps";
    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.cellSize = state.parameters.getDouble(base.push(P_CELL_SIZE), null);
        int decaySteps = state.parameters.getInt(base.push(P_DECAY), null);
        decay = decaySteps < 1 ? 0 : 1d / decaySteps;
    }    
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        int gridsize = (int) (sw.par.arenaSize / cellSize);
        visited = new double[gridsize][gridsize];
        accum = new double[gridsize][gridsize];
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SwarmPlayground agg = (SwarmPlayground) sim;
        for (SwarmAgent a : agg.agents) {
            Double2D loc = a.getLocation();
            visited[(int) (loc.x / cellSize)][(int) (loc.y / cellSize)] = 1;    
        }
        for(int i = 0 ; i < visited.length ; i++) {
            for(int j = 0 ; j < visited.length ; j++) {
                accum[i][j] += visited[i][j];
                if(decay > 0) { // decay cell value
                    visited[i][j] = Math.max(0, visited[i][j] - decay);
                }
            }
        }
    }

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        double total = 0;
        for(int i = 0 ; i < accum.length ; i++) {
            for(int j = 0 ; j < accum.length ; j++) {
                total += accum[i][j];
            }
        }
        return total / (accum.length * accum.length) / currentEvaluationStep;
    }
        
    
}
