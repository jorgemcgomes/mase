/*
 * To change this template, choose Tools | Templates
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
import mase.ExpandedFitness;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestSolutionGenStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    File bestsFolder;
    File outFile;
    public static final String P_COMPRESS = "compress";
    protected boolean compress;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
        outFile = new File(outFile.getParent(), prefix + outFile.getName());
        compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, true);
        if (compress) {
            try {
                bestsFolder = File.createTempFile("tempbests", Long.toString(System.currentTimeMillis()));
                bestsFolder.delete();
            } catch (IOException ex) {
                Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            bestsFolder = outFile;
        }
        if (!bestsFolder.exists()) {
            bestsFolder.mkdirs();
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        double bestFitness = Double.NEGATIVE_INFINITY;
        Individual best = null;
        int sub = -1;
        int index = -1;
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                float fit = ((ExpandedFitness) ind.fitness).fitnessScore();
                if (fit > bestFitness) {
                    bestFitness = fit;
                    best = ind;
                    sub = i;
                    index = j;
                }
            }
        }
        super.writeSolution(best, new File(bestsFolder,
                String.format("%03d", state.generation) + "_"
                + String.format("%02d", sub) + "_"
                + String.format("%03d", index) + "_"
                + String.format("%.2f", bestFitness) + ".ind"));
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        if (compress) {
            SolutionWriterStat.compressFolder(bestsFolder, outFile, true);
        }
    }
}
