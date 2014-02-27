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
import mase.PostEvaluator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PMCNS implements PostEvaluator {

    public static final String P_PERCENTILE = "percentile";
    public static final double DISAPPEAR = 0.0001;
    protected double percentile;
    protected int[] aptCount;
    protected double[] cutPoint;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.percentile = state.parameters.getDouble(base.push(P_PERCENTILE), null);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        if(aptCount == null) {
            aptCount = new int[pop.subpops.length];
            cutPoint = new double[pop.subpops.length];
        }
        for (int i = 0 ; i < pop.subpops.length ; i++) {
            Subpopulation sub = pop.subpops[i];
            DescriptiveStatistics ds = new DescriptiveStatistics(sub.individuals.length);
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                ds.addValue(nf.getFitnessScore());
            }
            cutPoint[i] = ds.getPercentile(percentile);
            aptCount[i] = 0;
            for(Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                if(nf.getFitnessScore() > cutPoint[i]) {
                    aptCount[i]++;
                    nf.setFitness(state, (float) nf.getNoveltyScore(), false);
                } else {
                    nf.setFitness(state, (float) (nf.getNoveltyScore() * DISAPPEAR), false);
                }
            }
        }
    }
}
