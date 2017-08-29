/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MinimalNoveltyCriteriaStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);
        if (statisticsFile != null) {
            try {
                log = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        MinimalNoveltyCriteria mcn = null;
        for(PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if(pe instanceof MinimalNoveltyCriteria) {
                mcn = (MinimalNoveltyCriteria) pe;
                break;
            }
        }
        state.output.print(state.generation + "", log);
        for (Subpopulation sub : state.population.subpops) {
            double maxFit = Float.NEGATIVE_INFINITY;
            DescriptiveStatistics dsNov = new DescriptiveStatistics(sub.individuals.size());
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                maxFit = Math.max(maxFit, nf.getFitnessScore());
                dsNov.addValue(nf.getScore(mcn.noveltyScore));
            }
            int boostCount = 0;
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                if (nf.fitness() > maxFit) {
                    boostCount++;
                }
            }
            state.output.print(" " + dsNov.getMean() + " " + 
                    dsNov.getStandardDeviation() + " " + dsNov.getMax() + " " + boostCount, log);
        }
        state.output.print("\n", log);
    }
}
