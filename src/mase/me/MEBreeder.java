/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleBreeder;

/**
 *
 * @author Jorge
 */
public class MEBreeder extends SimpleBreeder {

    private static final long serialVersionUID = 1L;

    @Override
    public Population breedPopulation(EvolutionState state) {
        for (Subpopulation sub : state.population.subpops) {
            if (sub instanceof MESubpopulation) {
                MESubpopulation me = (MESubpopulation) sub;
                me.updateRepertoire(state);
                if (me.individuals.size() != me.batchSize) {
                    // some individuals will be lost, but this is irrelevant since they are
                    // already stored in the repertoire, and the repertoire should be the
                    // only source of individuals for breeding, not the population
                    // this only serves to set how many new individuals are generated (batch size)
                    me.truncate(me.batchSize);
                }
            }
        }
        return super.breedPopulation(state);
    }

}
