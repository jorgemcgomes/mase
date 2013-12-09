/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 *
 * @author jorge
 */
public class CompetitiveBestStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    public static final String P_KEEP_LAST = "keep-last";
    public static final String P_COMPRESS = "compress";
    protected TarArchiveOutputStream[] taos;
    protected File[] outFile;
    protected File[] last;
    protected boolean compress;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, true);
        int n = state.parameters.getInt(new Parameter("pop.subpops"), null);
        outFile = new File[n];
        for (int i = 0; i < n; i++) {
            outFile[i] = state.parameters.getFile(base.push(P_FILE), null);
            String newName = compress ? outFile[i].getName().replace(".tar.gz", "." + i + ".tar.gz")
                    : outFile[i].getName() + "." + i;
            outFile[i] = new File(outFile[i].getParent(), jobPrefix + newName);
        }
        if (compress) {
            taos = new TarArchiveOutputStream[n];
            for (int i = 0; i < n; i++) {
                try {
                    taos[i] = new TarArchiveOutputStream(
                            new GZIPOutputStream(
                                    new BufferedOutputStream(new FileOutputStream(outFile[i]))));
                } catch (IOException ex) {
                    Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!compress && !outFile[i].exists()) {
                outFile[i].mkdirs();
            }
        }
        if (state.parameters.getBoolean(base.push(P_KEEP_LAST), null, true)) {
            last = new File[n];
            for (int i = 0; i < n; i++) {
                last[i] = new File(outFile[i].getParent(), jobPrefix + "last." + i + ".ind");
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
            PersistentController c = SolutionWriterStat.createPersistentController(best, state.generation, i, index);
            try {
                if (compress) {
                    SolutionWriterStat.writeSolutionToTar(c, taos[i]);
                } else {
                    SolutionWriterStat.writeSolutionInFolder(c, outFile[i]);
                }
                if (last != null) {
                    SolutionWriterStat.writeSolution(c, last[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        if (compress) {
            for (int i = 0; i < state.population.subpops.length; i++) {
                try {
                    taos[i].close();
                } catch (IOException ex) {
                    Logger.getLogger(CompetitiveBestStat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
