/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class NEATPipeline extends BreedingPipeline {

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
    }

    @Override
    public int numSources() {
        return 0;
    }

    @Override
    public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        // Get scores from individuals
        Map<Chromosome, Double> scores = new HashMap<>();

        NEATSubpop sub = (NEATSubpop) state.population.subpops[subpopulation];
        for (Individual ind : sub.individuals) {
            NEATIndividual ni = (NEATIndividual) ind;
            scores.put(ni.getChromosome(), ni.fitness.fitness());
        }

        // Create fitness function
        PreEvaluatedFitnessFunction fit = new PreEvaluatedFitnessFunction(scores);

        // Update NEAT population
        Chromosome cs[] = new Chromosome[sub.individuals.length];
        for (int j = 0; j < sub.individuals.length; j++) {
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
            inds[start + j] = sub.species.newIndividual(state, 0);
            ((NEATIndividual) inds[start + j]).setChromosome((NEATChromosome) sub.getNEAT().population().genoTypes()[j]);
        }
        return sub.individuals.length;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(NEATSubpop.P_NEAT_BASE);
    }

    @Override
    public boolean produces(EvolutionState state, Population newpop, int subpopulation, int thread) {
        return state.population.subpops[subpopulation] instanceof NEATSubpop;
    }

    @Override
    public int typicalIndsProduced() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxChildProduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int minChildProduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int reproduce(int n, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread, boolean produceChildrenFromSource) {
        for (int q = start; q < n + start; q++) {
            inds[q] = (Individual) (inds[q].clone());
        }
        return n;
    }
}
