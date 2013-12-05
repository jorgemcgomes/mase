/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.novelty.NSGA2.Individual;

/**
 *
 * @author jorge
 */
public class NSGA2Stat extends Statistics {

    public static final String P_FILE = "file";
    protected int log = 0;
    protected NSGA2 evaluator;

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
            if (pe instanceof NSGA2) {
                evaluator = (NSGA2) pe;
                break;
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        List<Individual>[] inds = evaluator.allInds;
        for (int s = 0; s < inds.length; s++) {
            Collections.sort(inds[s], new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    return Double.compare(o2.score, o1.score);
                }
            });
            for (Individual i : inds[s]) {
                state.output.print(state.generation + " " + s + " " + i.score + " " + i.rank + " " + i.crowdingDistance, log);
                for (double o : i.objectives) {
                    state.output.print(" " + o, log);
                }
                state.output.println("", log);
            }
        }
    }

}
