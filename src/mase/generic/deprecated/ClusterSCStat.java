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
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;

/**
 *
 * @author jorge
 */
public class ClusterSCStat extends Statistics {

    public static final String P_FINAL_CLUSTERS = "final-clusters";
    public static final String P_GEN_CLUSTERS = "gen-clusters";
    private static final long serialVersionUID = 1L;
    private int genLog, finalLog;
    private ClusterSCPostEvaluator sc;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        System.out.println("");
        super.setup(state, base);
        Parameter df = new Parameter(SCPostEvaluator.P_STATECOUNT_BASE);
        File genLogFile = state.parameters.getFile(base.push(P_GEN_CLUSTERS), df.push(P_GEN_CLUSTERS));
        File finalLogFile = state.parameters.getFile(base.push(P_FINAL_CLUSTERS), df.push(P_FINAL_CLUSTERS));
        try {
            genLog = genLogFile != null ? state.output.addLog(genLogFile, true, false) : -1;
            finalLog = state.output.addLog(finalLogFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the cluster logs.");
        }
        PostEvaluator[] postEvals = ((MetaEvaluator) state.evaluator).getPostEvaluators();
        for (PostEvaluator pe : postEvals) {
            if (pe instanceof ClusterSCPostEvaluator) {
                sc = (ClusterSCPostEvaluator) pe;
                break;
            }
        }
        if (sc == null) {
            state.output.fatal("No ClusterSCPostEvaluator to log.");
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        if (genLog != -1) {
            // cluster count
            float[] clusterCount = new float[sc.numClusters];
            for (Subpopulation sub : state.population.subpops) {
                for (Individual ind : sub.individuals) {
                    for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                        if (er instanceof SCResult) {
                            SCResult r = (SCResult) er;
                            for(int i = 0 ; i < sc.numClusters ; i++) {
                                clusterCount[i] += r.rawClusteredCount[i];
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < sc.numClusters; i++) {
                state.output.print(state.generation + " " + sc.counts[i] + " " + clusterCount[i], genLog);
                for (int j = 0; j < sc.clusters[i].length; j++) {
                    state.output.print(" " + sc.clusters[i][j], genLog);
                }
                state.output.println("", genLog);
            }
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        // cluster count
            float[] clusterCount = new float[sc.numClusters];
            for (Subpopulation sub : state.population.subpops) {
                for (Individual ind : sub.individuals) {
                    for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                        if (er instanceof SCResult) {
                            SCResult r = (SCResult) er;
                            for(int i = 0 ; i < sc.numClusters ; i++) {
                                clusterCount[i] += r.rawClusteredCount[i];
                            }
                        }
                    }
                }
            }
        for (int i = 0; i < sc.numClusters; i++) {
            state.output.print(sc.counts[i] + " " + clusterCount[i], finalLog);
            for (int j = 0; j < sc.clusters[i].length; j++) {
                state.output.print(" " + sc.clusters[i][j], finalLog);
            }
            state.output.println("", finalLog);
        }
    }
}
