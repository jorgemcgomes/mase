/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
import mase.PostEvaluator;
import mase.novelty.NoveltyFitness;

/**
 *
 * @author jorge
 */
public class LinearScalarization implements PostEvaluator {

    protected double blend;
    public static final String P_BLEND = "novelty-proportion";

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.blend = state.parameters.getDouble(base.push(P_BLEND), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (int i = 0; i < pop.subpops.length; i++) {
            // normalization
            float noveltyMin = Float.POSITIVE_INFINITY;
            float noveltyMax = Float.NEGATIVE_INFINITY;
            float fitnessMin = Float.POSITIVE_INFINITY;
            float fitnessMax = Float.NEGATIVE_INFINITY;
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                fitnessMin = (float) Math.min(fitnessMin, nf.getFitnessScore());
                fitnessMax = (float) Math.max(fitnessMax, nf.getFitnessScore());
                noveltyMin = (float) Math.min(noveltyMin, nf.getNoveltyScore());
                noveltyMax = (float) Math.max(noveltyMax, nf.getNoveltyScore());
            }

            // mix
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                float normalizedFitnessScore = fitnessMax == fitnessMin ? 0 : (nf.getFitnessScore() - fitnessMin) / (fitnessMax - fitnessMin);
                float normalizedNoveltyScore = (float) (noveltyMax == noveltyMin ? 0 : (nf.getNoveltyScore() - noveltyMin) / (noveltyMax - noveltyMin));
                nf.setFitness(state, (float) ((1 - blend) * normalizedFitnessScore + blend * normalizedNoveltyScore), false);
            }
        }
    }
}
