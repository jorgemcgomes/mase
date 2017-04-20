/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ExplorationFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;
    public static final String P_DISCRETISATION = "discretisation";
    private double discretisation;
    private boolean[][] visited;
    private FitnessResult fr;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.discretisation = state.parameters.getDouble(base.push(P_DISCRETISATION), null);
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        Playground pl = (Playground) sim;
        int gridsize = (int) (pl.par.arenaSize / discretisation);
        visited = new boolean[gridsize][gridsize];
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        Playground pl = (Playground) sim;
        Double2D loc = pl.agent.getLocation();
        visited[(int) (loc.x / discretisation)][(int) (loc.y / discretisation)] = true;
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        int count = 0;
        for(boolean[] bs : visited) {
            for(boolean b : bs) {
                if(b) {
                    count++;
                }
            }
        }
        fr = new FitnessResult(count / ((double) visited.length * visited.length));
    }
    

    @Override
    public FitnessResult getResult() {
        return fr;
    }
    
}
