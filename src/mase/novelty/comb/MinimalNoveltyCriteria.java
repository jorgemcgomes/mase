/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 * ONLY WORKS WITH ONE NOVELTY SCORE
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MinimalNoveltyCriteria implements PostEvaluator {

    public static final String P_NOVELTY_THRESHOLD = "novelty-threshold";
    public static final String P_NOVELTY_SCORE = "novelty-score";
    private static final long serialVersionUID = 1L;

    protected double noveltyThreshold;
    protected String noveltyScore;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.noveltyThreshold = state.parameters.getDouble(base.push(P_NOVELTY_THRESHOLD), null);
        this.noveltyScore = state.parameters.getString(base.push(P_NOVELTY_SCORE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (Subpopulation sub : pop.subpops) {
            // calculate average novelty inside subpop and max fitness
            double avgNovelty = 0;
            double maxFit = Float.NEGATIVE_INFINITY;
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                avgNovelty += nf.getScore(noveltyScore);
                maxFit = Math.max(maxFit, nf.getFitnessScore());
            }
            avgNovelty /= sub.individuals.length;

            // assign final scores
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                if (nf.getScore(noveltyScore) > avgNovelty * noveltyThreshold) {
                    nf.setFitness(state, maxFit + nf.getFitnessScore(), false);
                } else {
                    nf.setFitness(state, nf.getFitnessScore(), false);
                }
            }
        }
    }
}
