/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Population;
import ec.simple.SimpleBreeder;

/**
 *
 * @author Jorge
 */
public class MEBreeder extends SimpleBreeder {

    private static final long serialVersionUID = 1L;

    @Override
    public Population breedPopulation(EvolutionState state) {
        MESubpopulation me = (MESubpopulation) state.population.subpops[0];
        me.updateRepertoire(state);
        if (me.individuals.length != me.batchSize) {
            // some individuals will be lost, but this is irrelevant since they are
            // already stored in the repertoire
            me.resize(me.batchSize);
        }
        return super.breedPopulation(state);
    }

}
