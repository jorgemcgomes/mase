/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class SelectiveFitness implements PostEvaluator {

    public static final String P_FORCE_FITNESS = "force-fitness";
    protected int forceFitness;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.forceFitness = state.parameters.getInt(base.push(P_FORCE_FITNESS), null);
    }
    
    @Override
    public void processPopulation(EvolutionState state) {
        for(int i = 0 ; i < state.population.subpops.length ; i++) {
            if(i == forceFitness) {
                for(Individual ind : state.population.subpops[i].individuals) {
                    ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                    ef.setFitness(state, ef.getFitnessScore(), false);
                } 
            }
        }
    }
}
