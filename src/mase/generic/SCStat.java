/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import mase.EvaluationResult;
import mase.ExpandedFitness;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.evaluation.AgentEvaluationResult;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SCStat extends Statistics {

    public static final String P_LOG_FILE = "log-file";
    public static final String P_STATES_FILE = "states-file";
    private int genLog, statesLog;
    private SCPostEvaluator sc;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        System.out.println("");
        super.setup(state, base);
        Parameter df = new Parameter(SCPostEvaluator.P_STATECOUNT_BASE);
        File logFile = state.parameters.getFile(base.push(P_LOG_FILE), df.push(P_LOG_FILE));
        File statesFile = state.parameters.getFile(base.push(P_STATES_FILE), df.push(P_STATES_FILE));
        try {
            genLog = state.output.addLog(logFile, true, false);
            statesLog = state.output.addLog(statesFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the state count logs.");
        }
        PostEvaluator[] postEvals = ((MetaEvaluator) state.evaluator).getPostEvaluators();
        for (PostEvaluator pe : postEvals) {
            if (pe instanceof SCPostEvaluator) {
                sc = (SCPostEvaluator) pe;
                break;
            }
        }
        if (sc == null) {
            state.output.fatal("No StateCountPostEvaluator to log.");
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        DescriptiveStatistics filterDS = new DescriptiveStatistics();
        DescriptiveStatistics sizeDS = new DescriptiveStatistics();
        DescriptiveStatistics countDS = new DescriptiveStatistics();
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof SCResult) {
                        SCResult r = (SCResult) er;
                        filterDS.addValue(r.removedByFilter / (double) (r.removedByFilter + r.counts.size()));
                        sizeDS.addValue(r.counts.size());
                        for (Float f : r.counts.values()) {
                            countDS.addValue(f);
                        }
                    } 
                    /*else if (er instanceof AgentEvaluationResult) {
                        EvaluationResult[] ers = ((AgentEvaluationResult) er).getAllEvaluations();
                        for (EvaluationResult suber : ers) {
                            if (suber instanceof SCResult) {
                                SCResult r = (SCResult) suber;
                                filterDS.addValue((double) r.removedByFilter / (r.removedByFilter + r.counts.size()));
                                sizeDS.addValue(r.counts.size());
                                for (Float f : r.counts.values()) {
                                    countDS.addValue(f);
                                }
                            }
                        }
                    }*/
                }
            }
        }
        state.output.println(state.generation + " " + sc.globalCount.size() + " "
                + sizeDS.getMin() + " " + sizeDS.getMean() + " " + sizeDS.getMax() + " "
                + filterDS.getMin() + " " + filterDS.getMean() + " " + filterDS.getMax() + " "
                + countDS.getMin() + " " + countDS.getMean() + " " + countDS.getMax() + " " + countDS.getSkewness(), genLog);
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        // Sort by frequency
        ArrayList<Integer> hashes = new ArrayList<Integer>(sc.globalKey.keySet());
        Collections.sort(hashes, new Comparator<Integer>() {
            @Override
            public int compare(Integer h1, Integer h2) {
                return Float.compare(sc.globalCount.get(h2), sc.globalCount.get(h1));
            }
        });
        // Print visited states
        for (Integer h : hashes) {
            state.output.print(h + " " + sc.globalCount.get(h), statesLog);
            for (byte b : sc.globalKey.get(h)) {
                state.output.print(" " + b, statesLog);
            }
            state.output.print("\n", statesLog);
        }
    }
}
