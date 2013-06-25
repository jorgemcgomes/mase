/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import mase.PostEvaluator;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MCNovelty implements PostEvaluator {

    public static final String P_NOVELTY_THRESHOLD = "novelty-threshold";
    protected double noveltyThreshold;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.noveltyThreshold = state.parameters.getDouble(base.push(P_NOVELTY_THRESHOLD), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (Subpopulation sub : pop.subpops) {
            // calculate average novelty inside subpop and max fitness
            double avgNovelty = 0;
            float maxFit = Float.NEGATIVE_INFINITY;
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                avgNovelty += nf.noveltyScore;
                maxFit = Math.max(maxFit, nf.getFitnessScore());
            }
            avgNovelty /= sub.individuals.length;

            // assign final scores
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                if (nf.noveltyScore > avgNovelty * noveltyThreshold) {
                    nf.setFitness(state, maxFit + nf.getFitnessScore(), false);
                } else {
                    nf.setFitness(state, nf.getFitnessScore(), false);
                }
            }
        }
    }
}
