/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.MaseProblem;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class StateCountStat extends Statistics {

    public static final String P_LOG_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int genLog;
    private int evalIdx;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        System.out.println("");
        super.setup(state, base);
        try {
            File logFile = state.parameters.getFile(base.push(P_LOG_FILE), null);
            genLog = state.output.addLog(logFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the state count logs.");
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        evalIdx = -1;
        MaseProblem mp = (MaseProblem) state.evaluator.p_problem;
        for (int i = 0; i < mp.getEvalFunctions().length; i++) {
            if (mp.getEvalFunctions()[i] instanceof StateCountEvaluator) {
                evalIdx = i;
                break;
            }
        }
        state.output.println("Generation Subpop Index OriginalStates FilteredStates StateVisitsAfterFilter", genLog);
    }

    /**
     * Generation | Total unique states | Unique states this gen | total count
     * this gen | mean filtered (absolute) | min/mean/max filtered (relative) |
     * min/mean/max unique states in eval
     *
     * @param state
     */
    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                ExpandedFitness fit = (ExpandedFitness) ind.fitness;
                StateCountResult sr = (StateCountResult) fit.getCorrespondingEvaluation(evalIdx);
                int totalCount = 0;
                for (Integer c : sr.value().values()) {
                    totalCount += c;
                }
                state.output.println(state.generation + " " + i + " " + j + " " + sr.originalSize + " " + sr.value().size() + " " + totalCount, genLog);
            }
        }
    }
}
