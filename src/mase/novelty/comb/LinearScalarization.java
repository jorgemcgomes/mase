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
import java.util.Arrays;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class LinearScalarization implements PostEvaluator {

    public static final String P_SCORES = "scores";
    public static final String P_WEIGHTS = "weights";
    public static final String P_NORMALISE = "normalise";
    
    protected String[] scores;
    protected double[] weights;
    protected boolean normalise;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        String ws = state.parameters.getString(base.push(P_WEIGHTS), null);
        String[] wsA = ws.split("[,;\\s\\-]+");
        weights = new double[wsA.length];
        for(int i = 0 ; i < wsA.length ; i++) {
            weights[i] = Double.parseDouble(wsA[i]);
        }
        String ss = state.parameters.getString(base.push(P_SCORES), null);
        scores = ss.split("[,;\\s\\-]+");
        this.normalise = state.parameters.getBoolean(base.push(P_NORMALISE), null, true);
        if(scores.length != weights.length || scores.length < 1) {
            state.output.fatal("Number of scores and weights must be the same.", base.push(P_SCORES), base.push(P_WEIGHTS));
        }
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        for (Subpopulation subpop : pop.subpops) {
            // normalization
            double[] mins = new double[scores.length];
            Arrays.fill(mins, Double.POSITIVE_INFINITY);
            double[] maxs = new double[scores.length];
            Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
            for (Individual individual : subpop.individuals) {
                ExpandedFitness nf = (ExpandedFitness) individual.fitness;
                for(int k = 0 ; k < scores.length ; k++) {
                    double score = nf.getScore(scores[k]);
                    mins[k] = Math.min(mins[k], score);
                    maxs[k] = Math.max(maxs[k], score);
                }
            }
            // mix
            for (Individual individual : subpop.individuals) {
                ExpandedFitness nf = (ExpandedFitness) individual.fitness;
                double mixed = 0;
                for(int k = 0 ; k < scores.length ; k++) {
                    double score = nf.getScore(scores[k]);
                    if(normalise) {
                        if(mins[k] == maxs[k]) {
                            score = 0;
                        } else {
                            score = (score - mins[k]) / (maxs[k] - mins[k]);
                        }
                    }
                    mixed += weights[k] * score;
                }
                nf.setFitness(state, mixed, false);
            }
        }
    }
}
