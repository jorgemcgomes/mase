/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class SelectivePopScoring implements PostEvaluator {

    public static final String P_SCORES = "scores";
    private static final long serialVersionUID = 1L;
    protected String[] popScores;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        String force = state.parameters.getString(base.push(P_SCORES), null);
        this.popScores = force.split(",");
    }

    @Override
    public void processPopulation(EvolutionState state) {
        if(popScores.length < state.population.subpops.length) {
            state.output.fatal("More subpopulations (" + state.population.subpops.length + ") than scores (" + popScores.length +")");
        } else if(popScores.length > state.population.subpops.length) {
            state.output.warning("More scores (" + popScores.length + ") than populations (" + state.population.subpops.length +")");
        }
        for(int i = 0 ; i < state.population.subpops.length ; i++) {
            for(Individual ind : state.population.subpops[i].individuals) {
                ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                ef.setFitness(state, ef.getScore(popScores[i]), false);
            }
        }
    }
}
