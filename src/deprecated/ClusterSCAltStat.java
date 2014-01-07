/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.generic;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.MetaEvaluator;
import mase.PostEvaluator;

/**
 *
 * @author jorge
 */
public class ClusterSCAltStat extends Statistics {

    public static final String P_FILE = "file";
    protected int log = 0;
    protected ClusterSCPostEvalAlt evaluator;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File corrFile = state.parameters.getFile(base.push(P_FILE), null);
        if (corrFile != null) {
            try {
                log = state.output.addLog(corrFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + corrFile + ":\n" + i);
            }
        }
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof ClusterSCPostEvalAlt) {
                evaluator = (ClusterSCPostEvalAlt) pe;
                break;
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        if (evaluator != null) {
            state.output.print(state.generation + " W", log);
            for (int i = 0; i < evaluator.weightVector.length; i++) {
                state.output.print(" " + evaluator.weightVector[i], log);
            }
            state.output.print(" C", log);
            for (int i = 0; i < evaluator.countVector.length; i++) {
                state.output.print(" " + evaluator.countVector[i], log);
            }
            state.output.print(" T", log);    
            for (int i = 0; i < evaluator.totalCounts.length; i++) {
                state.output.print(" " + evaluator.totalCounts[i], log);
            }            
            state.output.println("", log);
        }

    }
    
    
    
}
