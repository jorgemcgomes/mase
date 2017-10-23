/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import java.io.File;
import java.io.IOException;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;

/**
 *
 * @author Jorge
 */
public class ArchiveTextStat extends Statistics {

    public static final String P_FILE = "file";
    public static final String P_UPDATE_ALWAYS = "update-always";
    private static final long serialVersionUID = 1L;
    private boolean updateAlways;
    private File logFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        logFile = state.parameters.getFile(base.push(P_FILE), null);
        updateAlways = state.parameters.getBoolean(base.push(P_UPDATE_ALWAYS), null, false);
    }

    @Override
    public void preBreedingStatistics(EvolutionState state) {
        super.preBreedingStatistics(state);
        if (updateAlways) {
            logArchive(state);
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        logArchive(state);
    }

    private void logArchive(EvolutionState state) {
        try {
            int log = state.output.addLog(logFile, true);
            NoveltyEvaluation ne = null;
            for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
                if (pe instanceof NoveltyEvaluation) {
                    ne = (NoveltyEvaluation) pe;
                    break;
                }
            }
            boolean headed = false;

            for (int a = 0; a < ne.archives.length; a++) {
                for (int x = 0; x < ne.archives[a].size(); x++) {
                    ArchiveEntry e = ne.archives[a].get(x);
                    String behav = e.getBehaviour().toString();
                    double[] genome = e.getIndividual() instanceof DoubleVectorIndividual ? ((DoubleVectorIndividual) e.getIndividual()).genome : new double[0];
                    if (!headed) { // add file header
                        state.output.print("Archive Index Fitness", log);
                        for (int i = 0; i < behav.split(" ").length; i++) {
                            state.output.print(" Behav_" + i, log);
                        }
                        for (int i = 0; i < genome.length; i++) {
                            state.output.print(" Genome_" + i, log);
                        }
                        state.output.println("", log);
                        headed = true;
                    }

                    state.output.print(a + " " + x + " " + e.getFitness() + " " + behav, log);
                    for (double g : genome) {
                        state.output.print(" " + g, log);
                    }
                    state.output.println("", log);
                }
            }
        } catch (IOException ex) {
            state.output.fatal("An IOException occurred while trying to create the log " + logFile);
        }
    }
}
