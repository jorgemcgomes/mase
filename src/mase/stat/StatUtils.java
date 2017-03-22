/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class StatUtils {

    protected static class IndividualInfo {

        final Individual ind;
        final int sub;
        final int index;
        final double fitness;

        protected IndividualInfo(Individual ind, int subpop, int index) {
            this.ind = ind;
            this.sub = subpop;
            this.index = index;
            this.fitness = ((ExpandedFitness) ind.fitness).getFitnessScore();
        }
    }

    public static IndividualInfo getBest(EvolutionState state) {
        Individual best = null;
        int sub = -1;
        int index = -1;
        double maxFit = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                double fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > maxFit) {
                    maxFit = fit;
                    best = ind;
                    sub = i;
                    index = j;
                }
            }
        }
        return new IndividualInfo(best, sub, index);
    }

    public static IndividualInfo[] getSubpopBests(EvolutionState state) {
        IndividualInfo[] res = new IndividualInfo[state.population.subpops.length];
        for (int i = 0; i < state.population.subpops.length; i++) {
            Individual best = null;
            int sub = -1;
            int index = -1;
            double maxFit = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                double fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > maxFit) {
                    maxFit = fit;
                    best = ind;
                    sub = i;
                    index = j;
                }
            }
            res[i] = new IndividualInfo(best, sub, index);
        }
        return res;
    }
}
