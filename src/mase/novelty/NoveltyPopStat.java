/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.MetaEvaluator;
import mase.PostEvaluator;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyPopStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
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
        NoveltyEvaluation ne = null;
        for(PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if(pe instanceof NoveltyEvaluation) {
                ne = (NoveltyEvaluation) pe;
                break;
            }
        }
                
        state.output.print(state.generation + " " + ne.archive.size(), log);
        
        for (int i = 0; i < state.population.subpops.length; i++) {
            double averageNovScore = 0;
            double maxNovScore = Double.NEGATIVE_INFINITY;
            double minNovScore = Double.POSITIVE_INFINITY;
            double avgFromRepo = 0;
            double maxFromRepo = Double.NEGATIVE_INFINITY;
            double minFromRepo = Double.POSITIVE_INFINITY;
            
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) state.population.subpops[i].individuals[j].fitness;
                averageNovScore += nf.noveltyScore;
                avgFromRepo += nf.repoComparisons;
                maxNovScore = Math.max(maxNovScore, nf.noveltyScore);
                minNovScore = Math.min(minNovScore, nf.noveltyScore);
                maxFromRepo = Math.max(maxFromRepo, nf.repoComparisons);
                minFromRepo = Math.min(minFromRepo, nf.repoComparisons);
            }
            
            avgFromRepo /= state.population.subpops[i].individuals.length;
            averageNovScore /= state.population.subpops[i].individuals.length;
            
            state.output.print(" " + averageNovScore + " " + maxNovScore + " " + minNovScore + " " +
                    avgFromRepo + " " + maxFromRepo + " " + minFromRepo, log);
        }
        state.output.print("\n", log);
    }
}
