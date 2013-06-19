/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class StochasticNovelty extends NoveltyEvaluation {

    public static final String P_NOVELTY_PROB = "novelty-prob";
    protected double noveltyProb;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        noveltyProb = state.parameters.getDouble(base.push(P_NOVELTY_PROB), null);
    }
    
    @Override
    protected void setFinalScores(EvolutionState state, Population pop) {
        for(Subpopulation sub : pop.subpops) {
            boolean novelty = state.random[0].nextBoolean(noveltyProb);
            for(Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                if(novelty) {
                    nf.setFitness(state, (float) nf.noveltyScore, false);
                } else {
                    nf.setFitness(state, nf.getFitnessScore(), false);
                }
            }
        }
    }
    
    
    
}
