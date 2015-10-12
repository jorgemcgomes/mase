/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class LeapScoring implements PostEvaluator {

    public static final String P_CHANGE_PROB = "change-prob";
    public static final String P_BASE_SCORE = "base-score";
    public static final String P_LEAP_SCORE = "leap-score";
    protected String baseScore, leapScore;
    protected double changeProb;
    protected int currentLeap;
    

    @Override
    public void setup(EvolutionState state, Parameter base) {
        // the mean number of generations it will stay on each subpop is roughly 1/changeProb and stdev is roughly the same as the mean
        this.changeProb = state.parameters.getDouble(base.push(P_CHANGE_PROB), null);
        this.currentLeap = -1;
        baseScore = state.parameters.getString(base.push(P_BASE_SCORE), null);
        leapScore = state.parameters.getString(base.push(P_LEAP_SCORE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        boolean change = currentLeap == -1 ? true : state.random[0].nextBoolean(changeProb);
        if (change) {
            int previousNovelty = currentLeap;
            do {
                currentLeap = state.random[0].nextInt(pop.subpops.length);
            } while (currentLeap == previousNovelty);
        }

        for (int s = 0; s < pop.subpops.length; s++) {
            for (Individual ind : pop.subpops[s].individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                if (s == currentLeap) {
                    nf.setFitness(state, nf.getScore(leapScore), false);
                } else {
                    nf.setFitness(state, nf.getScore(baseScore), false);
                }
            }
        }
    }
}
