/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Population;
import ec.simple.SimpleInitializer;
import ec.util.Parameter;
import org.neat4j.neat.core.InnovationDatabase;

/**
 *
 * @author jorge
 */
public class NEATInitializer extends SimpleInitializer {

    public static final String P_SHARED_DB = "shared-database";
    protected InnovationDatabase innovDB = null;
    protected boolean shareDB;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        shareDB = state.parameters.getBoolean(new Parameter(NEATSubpop.P_NEAT_BASE).push(P_SHARED_DB), null, false);
    }

    @Override
    public Population setupPopulation(EvolutionState state, int thread) {
        if (shareDB) {
            innovDB = new InnovationDatabase();
        }
        return super.setupPopulation(state, thread);
    }

}
