/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import mase.PostEvaluator;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class LeapNovelty implements PostEvaluator {

    public static final String P_CHANGE_PROB = "change-prob";
    protected double changeProb;
    protected int currentNovelty;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        // the mean number of generations it will stay on each subpop is roughly 1/changeProb and stdev is roughly the same as the mean
        this.changeProb = state.parameters.getDouble(base.push(P_CHANGE_PROB), null);
        this.currentNovelty = -1;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        boolean change = currentNovelty == -1 ? true : state.random[0].nextBoolean(changeProb);
        if (change) {
            int previousNovelty = currentNovelty;
            do {
                currentNovelty = state.random[0].nextInt(pop.subpops.length);
            } while (currentNovelty == previousNovelty);
        }

        for (int s = 0; s < pop.subpops.length; s++) {
            for (Individual ind : pop.subpops[s].individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                if (s == currentNovelty) {
                    nf.setFitness(state, (float) nf.noveltyScore, false);
                } else {
                    nf.setFitness(state, nf.getFitnessScore(), false);
                }
            }
        }
    }
}
