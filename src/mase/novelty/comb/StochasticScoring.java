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
public class StochasticScoring implements PostEvaluator {

    public static final String P_BASE_SCORE = "base-score";
    public static final String P_ALT_SCORE = "alt-score";
    public static final String P_NOVELTY_PROB = "alt-prob";
    protected String baseScore, altScore;
    protected double altProb;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        altProb = state.parameters.getDouble(base.push(P_NOVELTY_PROB), null);
        baseScore = state.parameters.getString(base.push(P_BASE_SCORE), null);
        altScore = state.parameters.getString(base.push(P_ALT_SCORE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (Subpopulation sub : pop.subpops) {
            boolean alt = state.random[0].nextBoolean(altProb);
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                if (alt) {
                    nf.setFitness(state, nf.getScore(altScore), false);
                } else {
                    nf.setFitness(state, nf.getScore(baseScore), false);
                }
            }
        }
    }
}
