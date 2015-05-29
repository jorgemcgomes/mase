/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class AlternatingFitness implements PostEvaluator {

    public static final String P_FREQUENCY = "frequency";
    private int frequency;
    private int currentFitness;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        frequency = state.parameters.getInt(base.push(P_FREQUENCY), null);
        currentFitness = 0;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        for (int i = 0; i < state.population.subpops.length; i++) {
            if (i == currentFitness) {
                for (Individual ind : state.population.subpops[i].individuals) {
                    ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                    ef.setFitness(state, ef.getFitnessScore(), false);
                }
            }
        }
        if((state.generation + 1) % frequency == 0) {
            currentFitness = (currentFitness + 1) % state.population.subpops.length;
        }
    }

}
