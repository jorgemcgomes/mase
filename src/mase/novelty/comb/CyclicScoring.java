/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class CyclicScoring implements PostEvaluator {

    public static final String P_FREQUENCY = "frequency";
    public static final String P_BASE_SCORE = "base-score";
    public static final String P_CYCLING_SCORE = "cycling-score";
    protected String baseScore, cyclingScore;
    private int frequency;
    private int current;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        frequency = state.parameters.getInt(base.push(P_FREQUENCY), null);
        current = 0;
        baseScore = state.parameters.getString(base.push(P_BASE_SCORE), null);
        cyclingScore = state.parameters.getString(base.push(P_CYCLING_SCORE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (Individual ind : state.population.subpops[i].individuals) {
                ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                if (i == current) {
                    ef.setFitness(state, ef.getScore(cyclingScore), false);
                } else {
                    ef.setFitness(state, ef.getScore(baseScore), false);
                }
            }
        }
        if ((state.generation + 1) % frequency == 0) {
            current = (current + 1) % state.population.subpops.length;
        }
    }
}
