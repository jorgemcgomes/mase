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
import static mase.novelty.MCNoveltyStat.P_STATISTICS_FILE;

/**
 *
 * @author jorge
 */
public class WeightedNoveltyStat extends Statistics {

    public static final String P_FILE = "file";
    protected int log = 0;
    protected WeightedNovelty evaluator;

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
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof NoveltyEvaluation) {
                evaluator = (WeightedNovelty) pe;
                break;
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        state.output.print(state.generation + "", log);
        for (int i = 0; i < evaluator.weights.length; i++) {
            state.output.print(" " + evaluator.weights[i], log);
        }
        for (int i = 0; i < evaluator.weights.length; i++) {
            state.output.print(" " + evaluator.instantCorrelation[i], log);
        }
        state.output.println("", log);
    }
}
