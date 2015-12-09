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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PMCNS implements PostEvaluator {

    public static final String P_PERCENTILE = "percentile";
    public static final String P_CHANGE_RATE = "change-rate";
    public static final String P_NOVELTY_SCORE = "novelty-score";
    public static final double DISAPPEAR = 0.0001;
    private static final long serialVersionUID = 1L;
    protected double percentile;
    protected double changeRate;
    protected int[] aptCount;
    protected double[] cutPoint;
    protected String noveltyScore;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.percentile = state.parameters.getDouble(base.push(P_PERCENTILE), null);
        this.changeRate = state.parameters.getDouble(base.push(P_CHANGE_RATE), null);
        this.noveltyScore = state.parameters.getString(base.push(P_NOVELTY_SCORE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        if(aptCount == null) {
            aptCount = new int[pop.subpops.length];
            cutPoint = new double[pop.subpops.length];
            Arrays.fill(cutPoint, 0);
        }
        for (int i = 0 ; i < pop.subpops.length ; i++) {
            Subpopulation sub = pop.subpops[i];
            DescriptiveStatistics ds = new DescriptiveStatistics(sub.individuals.length);
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                ds.addValue(nf.getFitnessScore());
            }
            
            double p = ds.getPercentile(percentile);
            //cutPoint[i] = p * changeRate + cutPoint[i] * (1 - changeRate);
            cutPoint[i] = cutPoint[i] + Math.max(0, (p - cutPoint[i]) * changeRate);
            aptCount[i] = 0;
            for(Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                double ns = nf.getScore(noveltyScore);
                if(nf.getFitnessScore() > cutPoint[i]) {
                    aptCount[i]++;
                    nf.setFitness(state, ns , false);
                } else {
                    nf.setFitness(state, (ns * DISAPPEAR), false);
                }
            }
        }
    }
}
