/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.mo.NSGA2.NSGAIndividual;

/**
 *
 * @author jorge
 */
public class NSGA2Stat extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
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
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        state.output.print("Generation Subpop Scalarized Rank CrowdingDist", log);
        for(String o : evaluator.include) {
            state.output.print(" " + o, log);
        }
        state.output.println("", log);
    }
    
    

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        List<NSGAIndividual>[] inds = evaluator.indsRank;        
        for (int s = 0; s < inds.length; s++) {
            Collections.sort(inds[s], new Comparator<NSGAIndividual>() {
                @Override
                public int compare(NSGAIndividual o1, NSGAIndividual o2) {
                    return Double.compare(o2.score, o1.score);
                }
            });
            for (NSGAIndividual i : inds[s]) {
                state.output.print(state.generation + " " + s + " " + i.score + " " + i.rank + " " + i.crowdingDistance, log);
                for (double o : i.objectives) {
                    state.output.print(" " + o, log);
                }
                state.output.println("", log);
            }
        }
    }

}
