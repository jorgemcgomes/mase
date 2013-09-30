/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Subpopulation;
import ec.util.Parameter;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATPopulation;

/**
 *
 * @author jorge
 */
public class NEATSubpop extends Subpopulation {

    private TrainEA neat;

    // parameters
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // load neat parameters

    }

    public TrainEA getNEAT() {
        return neat;
    }

    @Override
    public void populate(EvolutionState state, int thread) {
        ((NEATPopulation) neat.getPopulation()).reset();
    }

    public void breed() {
        // evaluate
        for(Species spec : neat.getPopulation().getSpecies()) {
            int i = 0;
            for(Genome g : spec.getMembers()) {
                g.setScore(0); // TODO: get from super.individuals
            }
        }
        
        // breed 
        neat.iteration();
        
        
    }
}
