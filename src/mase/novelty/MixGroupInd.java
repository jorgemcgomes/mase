/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import mase.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 * GROUP = 1st score, IND = 2nd score
 *
 * @author jorge
 */
public class MixGroupInd implements PostEvaluator {

    
    public static final String GROUP_NOVELTY = "group-novelty", IND_NOVELTY = "ind-novelty";
    public static final String MIX_NOVELTY = "mix-novelty"; 
    public static final String P_GROUP_WEIGHT = "group-weight";
    protected double groupWeight;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.groupWeight = state.parameters.getDouble(base.push(P_GROUP_WEIGHT), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        for (Subpopulation subpop : state.population.subpops) {
            // Normalise both group and individual scores
            double indMin = Double.POSITIVE_INFINITY, indMax = Double.NEGATIVE_INFINITY, groupMin = Double.POSITIVE_INFINITY, groupMax = Double.NEGATIVE_INFINITY;
            for (Individual ind : subpop.individuals) {
                ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                double groupScore = ef.scores().get(GROUP_NOVELTY);
                double indScore = ef.scores().get(IND_NOVELTY);
                indMin = Math.min(indMin, indScore);
                indMax = Math.max(indMax, indScore);
                groupMin = Math.min(groupMin, groupScore);
                groupMax = Math.max(groupMax, groupScore);
            }
            // Mix individual scores and group scores
            for (Individual ind : subpop.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                double groupScore = groupMin == groupMax ? 0 : (nf.scores().get(GROUP_NOVELTY) - groupMin) / (groupMax - groupMin);
                double indScore = indMin == indMax ? 0 : (nf.scores().get(IND_NOVELTY) - indMin) / (indMax - indMin);
                double noveltyScore = (1 - groupWeight) * indScore + groupWeight * groupScore;
                nf.scores().put(MIX_NOVELTY, (float) noveltyScore);
                nf.setFitness(state, (float) noveltyScore, false);
            }
        }
    }

}
