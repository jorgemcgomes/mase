/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleBreeder;
import java.util.HashMap;
import java.util.Map;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class NEATBreeder extends SimpleBreeder {

    @Override
    public Population breedPopulation(EvolutionState state) {
        // Get scores from individuals
        Map<Chromosome, Double> scores = new HashMap<Chromosome, Double>();
        for (Subpopulation subpop : state.population.subpops) {
            NEATSubpop sub = (NEATSubpop) subpop;
            for (Individual ind : sub.individuals) {
                NEATIndividual ni = (NEATIndividual) ind;
                scores.put(ni.getChromosome(), ni.fitness.fitness());
            }
        }
        // Create fitness function
        PreEvaluatedFitnessFunction fit = new PreEvaluatedFitnessFunction(scores);

        Population newpop = (Population) state.population.emptyClone();
        for (int i = 0; i < state.population.subpops.length; i++) {
            NEATSubpop sub = (NEATSubpop) state.population.subpops[i];
            
            // Update NEAT population
            Chromosome cs[] = new Chromosome[sub.individuals.length];
            for(int j = 0 ; j < sub.individuals.length ; j++) {
                NEATIndividual ni = (NEATIndividual) sub.individuals[j];
                cs[j] = ni.getChromosome();
            }
            sub.getNEAT().population().updatePopulation(cs);
            
            // Plug in fitness function
            sub.getNEAT().pluginFitnessFunction(fit);

            // Run NEAT Epoch
            sub.getNEAT().runEpoch();

            // Update ECJ population
            for (int j = 0; j < sub.individuals.length; j++) {
                newpop.subpops[i].individuals[j] = sub.species.newIndividual(state, 0);
                ((NEATIndividual) newpop.subpops[i].individuals[j])
                        .setChromosome((NEATChromosome) sub.getNEAT().population().genoTypes()[j]);
            }
        }
        return newpop;
    }
}
