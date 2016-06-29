/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jorge
 */
public class ProblemInstanceStat extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(base.push(P_FILE), null);
        try {
            log = state.output.addLog(statisticsFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        AllocationProblem prob = (AllocationProblem) state.evaluator.p_problem;
        for(int i = 0 ; i < prob.types.length ; i++) {
            for(int j = 0 ; j < prob.types[i].length ; j++) {
                state.output.print(prob.types[i][j] + " ", log);
            }
            state.output.println("",log);
        }
    }

}
