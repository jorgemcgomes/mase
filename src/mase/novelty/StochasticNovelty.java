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
public class StochasticNovelty implements PostEvaluator {

    public static final String P_NOVELTY_PROB = "novelty-prob";
    protected double noveltyProb;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        noveltyProb = state.parameters.getDouble(base.push(P_NOVELTY_PROB), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (Subpopulation sub : pop.subpops) {
            boolean novelty = state.random[0].nextBoolean(noveltyProb);
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                if (novelty) {
                    nf.setFitness(state, (float) nf.noveltyScore, false);
                } else {
                    nf.setFitness(state, nf.getFitnessScore(), false);
                }
            }
        }
    }
}
