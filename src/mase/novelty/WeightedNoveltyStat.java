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

    public static final String P_CORR_FILE = "corr-file";
    public static final String P_WEIGHT_FILE = "weight-file";
    protected int corrLog = 0, weightLog = 0;
    protected WeightedNovelty evaluator;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File corrFile = state.parameters.getFile(base.push(P_CORR_FILE), null);
        if (corrFile != null) {
            try {
                corrLog = state.output.addLog(corrFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + corrFile + ":\n" + i);
            }
        }
        File weightFile = state.parameters.getFile(base.push(P_WEIGHT_FILE), null);
        if (weightFile != null) {
            try {
                weightLog = state.output.addLog(weightFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + weightFile + ":\n" + i);
            }
        }
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof WeightedNovelty) {
                evaluator = (WeightedNovelty) pe;
                break;
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        if (evaluator != null) {
            state.output.print(state.generation + "", weightLog);
            for (int i = 0; i < evaluator.weights.length; i++) {
                state.output.print(" " + evaluator.weights[i], weightLog);
            }
            state.output.println("", weightLog);
            state.output.print(state.generation + "", corrLog);
            for (int i = 0; i < evaluator.weights.length; i++) {
                state.output.print(" " + evaluator.instantCorrelation[i], corrLog);
            }
            state.output.println("", corrLog);
        }

    }
}
