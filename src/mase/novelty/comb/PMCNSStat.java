/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty.comb;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;

/**
 *
 * @author jorge
 */
public class PMCNSStat extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    protected int log = 0;
    protected PMCNS evaluator;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File file = state.parameters.getFile(base.push(P_FILE), null);
        if (file != null) {
            try {
                log = state.output.addLog(file, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + file + ":\n" + i);
            }
        }

        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof PMCNS) {
                evaluator = (PMCNS) pe;
                break;
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        
        state.output.print(state.generation + "", log);
        for(int i = 0 ; i < evaluator.aptCount.length ; i++) {
            state.output.print(" " + evaluator.aptCount[i] + " " + evaluator.cutPoint[i], log);
        }
        state.output.println("", log);
    }

}
