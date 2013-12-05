/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class CompetitiveBestStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    public static final String P_KEEP_LAST = "keep-last";
    public static final String P_COMPRESS = "compress";
    File[] bestsFolder;
    File[] outFile;
    File[] last;
    protected boolean compress;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, true);
        int n = state.parameters.getInt(new Parameter("pop.subpops"), null);
        outFile = new File[n];
        for (int i = 0; i < n; i++) {
            outFile[i] = state.parameters.getFile(base.push(P_FILE), null);
            if(compress) {
                outFile[i] = new File(outFile[i].getParent(), prefix + outFile[i].getName().replace(".tar.gz", i + ".tar.gz"));
            } else {
                outFile[i] = new File(outFile[i].getParent(), prefix + outFile[i].getName() + "." + i);
            }
        }
        if (compress) {
            bestsFolder = new File[n];
            for (int i = 0; i < n; i++) {
                try {
                    bestsFolder[i] = File.createTempFile("tempbests" + i, Long.toString(System.currentTimeMillis()));
                    bestsFolder[i].delete();
                } catch (IOException ex) {
                    Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            bestsFolder = outFile;
        }
        for (int i = 0; i < n; i++) {
            if (!bestsFolder[i].exists()) {
                bestsFolder[i].mkdirs();
            }
        }
        boolean k = state.parameters.getBoolean(base.push(P_KEEP_LAST), null, true);
        if (k) {
            last = new File[n];
            for (int i = 0; i < n; i++) {
                last[i] = new File(outFile[i].getParent(), prefix + "last" + i + ".ind");
            }
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        for (int i = 0; i < state.population.subpops.length; i++) {
            Individual best = null;
            int index = -1;
            double bestFitness = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                float fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > bestFitness) {
                    bestFitness = fit;
                    best = ind;
                    index = j;
                }
            }
            super.writeSolution(best, new File(bestsFolder[i],
                    String.format("%03d", state.generation) + "_"
                    + String.format("%02d", i) + "_"
                    + String.format("%03d", index) + "_"
                    + String.format("%.2f", bestFitness) + ".ind"));
            if (last != null) {
                super.writeSolution(best, last[i]);
            }
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        if (compress) {
            for (int i = 0; i < state.population.subpops.length; i++) {
                SolutionWriterStat.compressFolder(bestsFolder[i], outFile[i], true);
            }
        }
    }
}
