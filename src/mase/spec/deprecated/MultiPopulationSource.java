/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class MultiPopulationSource extends BreedingSource {

    public static final String P_MULTIPOP_SOURCE = "multipop-source";
    public static final String P_TOURNAMENT_SIZE = "tournament-size";
    private int tournamentSize;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.tournamentSize = state.parameters.getInt(base.push(P_TOURNAMENT_SIZE), defaultBase().push(P_TOURNAMENT_SIZE));
    }

    @Override
    public int typicalIndsProduced() {
        return 1;
    }

    // onl
    @Override
    public boolean produces(EvolutionState state, Population newpop, int subpopulation, int thread) {
        return true;
    }

    @Override
    public void prepareToProduce(EvolutionState state, int subpopulation, int thread) {
        // form pools -- warning: this can be called multiple times in the same generation
        // THIS IS A PROTOTYPE, NOT A SINGLETON
    }

    @Override
    public void finishProducing(EvolutionState s, int subpopulation, int thread) {
        // nothing to do here!
    }

    @Override
    public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        SelectionPoolBuilder spb = (SelectionPoolBuilder) state.exchanger;
        Individual[] pool = spb.getPool(subpopulation);
        int best = tournament(pool, state, thread);

        // stat
        if (best < state.population.subpops.get(subpopulation).individuals.size()) {
            spb.logPick(subpopulation);
        }

        inds[start] = pool[best];
        //System.out.println(best);
        return 1;
    }

    protected int tournament(Individual[] inds, EvolutionState state, int thread) {
        int best = state.random[thread].nextInt(inds.length);
        for (int x = 1; x < tournamentSize; x++) {
            int j = state.random[thread].nextInt(inds.length);
            if (inds[j].fitness.betterThan(inds[best].fitness)) {
                best = j;
            }
        }
        return best;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_MULTIPOP_SOURCE);
    }

}
