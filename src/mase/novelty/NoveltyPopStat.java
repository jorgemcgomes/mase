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
import java.util.ArrayList;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyPopStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    public int log = 0;  // stdout by default
    protected ArrayList<NoveltyEvaluation> neList = null;

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

        neList = new ArrayList<NoveltyEvaluation>();
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof NoveltyEvaluation) {
                neList.add((NoveltyEvaluation) pe);
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);

        state.output.print(state.generation + "", log);
        for (NoveltyEvaluation ne : neList) {
            for (int i = 0; i < state.population.subpops.length; i++) {
                double averageNovScore = 0;
                double maxNovScore = Double.NEGATIVE_INFINITY;
                double minNovScore = Double.POSITIVE_INFINITY;
                for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                    ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                    double nsScore = nf.scores().get(ne.scoreName);
                    averageNovScore += nsScore;
                    maxNovScore = Math.max(maxNovScore, nsScore);
                    minNovScore = Math.min(minNovScore, nsScore);
                }
                averageNovScore /= state.population.subpops[i].individuals.length;
                state.output.print(" " + ne.archives[i].size() + " " + minNovScore + " " + averageNovScore + " " + maxNovScore , log);
            }
        }
        state.output.println("", log);
    }
}
