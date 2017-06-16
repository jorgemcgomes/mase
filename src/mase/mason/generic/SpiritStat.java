/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SpiritStat extends Statistics {

    public static final String P_LOG_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int log;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        try {
            File logFile = state.parameters.getFile(base.push(P_LOG_FILE), null);
            log = state.output.addLog(logFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the state count logs.");
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        state.output.println("Generation Subpop Index VisitedPreFilter VisitedPostFilter GrandTotal P05 P10 P25 P50 P75 P90 P95 Max", log);
    }
    
    

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        int evalIdx = -1;
        MaseProblem mp = (MaseProblem) state.evaluator.p_problem;
        for (int i = 0; i < mp.getEvalFunctions().length; i++) {
            if (mp.getEvalFunctions()[i] instanceof SpiritEvaluator) {
                evalIdx = i;
                break;
            }
        }

        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                ExpandedFitness fit = (ExpandedFitness) ind.fitness;
                SpiritResult sr = (SpiritResult) fit.getCorrespondingEvaluation(evalIdx);
                DescriptiveStatistics ds = new DescriptiveStatistics();
                for (int t : sr.totalsBeforeFilter) {
                    if (t > 0) {
                        ds.addValue(t);
                    }
                }
                state.output.println(state.generation + " " + i + " " + j + " "
                        + sr.visitedBeforeFilter + " " + sr.visitedAfterFilter + " " + sr.grandTotalBeforeFilter + " " +
                        + ds.getPercentile(5) + " " + ds.getPercentile(10) + " " + ds.getPercentile(25) + " "
                        + ds.getPercentile(50) + " " + ds.getPercentile(75) + " " + ds.getPercentile(90) + " "
                        + ds.getPercentile(95) + " " + ds.getMax(), log);
            }
        }
    }

}
