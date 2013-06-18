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

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SampleSolutionsStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    public static final String P_SAMPLE_SIZE = "sample-size";
    public static final String P_COMPRESS = "compress";
    protected boolean compress;
    protected int sampleSize;
    File folder;
    File outFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
        outFile = new File(outFile.getParent(), prefix + outFile.getName());
        compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, true);
        if (compress) {
            try {
                folder = File.createTempFile("tempsample", Long.toString(System.currentTimeMillis()));
                folder.delete();
            } catch (IOException ex) {
                Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            folder = outFile;
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        sampleSize = state.parameters.getInt(base.push(P_SAMPLE_SIZE), null);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        int[] subs = new int[sampleSize];
        int[] inds = new int[sampleSize];
        for (int i = 0; i < sampleSize;) {
            int sub = state.random[0].nextInt(state.population.subpops.length);
            int ind = state.random[0].nextInt(state.population.subpops[sub].individuals.length);
            for (int j = 0; j < i; j++) { // check if this individual was already picked
                if (subs[j] == sub && inds[j] == ind) {
                    continue;
                }
            }
            subs[i] = sub;
            inds[i] = ind;
            i++;
        }

        for (int i = 0; i < sampleSize; i++) {
            Individual ind = state.population.subpops[subs[i]].individuals[inds[i]];
            super.writeSolution(ind, new File(folder,
                    String.format("%03d", state.generation) + "_"
                    + String.format("%02d", subs[i]) + "_"
                    + String.format("%03d", inds[i]) + "_"
                    + String.format("%.2f", ind.fitness.fitness()) + ".ind"));
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        if(compress) {
            SolutionWriterStat.compressFolder(folder, outFile, true);
        }
    }
}
