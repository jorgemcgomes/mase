/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SampleSolutionsStat extends Statistics {

    public static final String P_FILE = "file";
    public static final String P_SAMPLE_SIZE = "sample-size";
    private static final long serialVersionUID = 1L;
    protected int sampleSize;
    protected File outFile;
    protected transient TarArchiveOutputStream taos;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
        try {
            int l = state.output.addLog(outFile, false);
            outFile = state.output.getLog(l).filename;
            taos = new TarArchiveOutputStream(
                    new GZIPOutputStream(
                            new BufferedOutputStream(new FileOutputStream(outFile))));
        } catch (IOException ex) {
            Logger.getLogger(SampleSolutionsStat.class.getName()).log(Level.SEVERE, null, ex);
        }

        sampleSize = state.parameters.getInt(base.push(P_SAMPLE_SIZE), null);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        if (taos == null) { // can happen in case of resuming from checkpoint
            taos = SolutionPersistence.reopenTar(outFile);
        }

        int[] subs = new int[sampleSize];
        int[] inds = new int[sampleSize];
        int added = 0;
        while (added < sampleSize) {
            int sub = state.random[0].nextInt(state.population.subpops.length);
            int ind = state.random[0].nextInt(state.population.subpops[sub].individuals.length);
            boolean repeated = false;
            for (int j = 0; j < added; j++) { // check if this individual was already picked
                if (subs[j] == sub && inds[j] == ind) {
                    repeated = true;
                    break;
                }
            }
            if (!repeated) {
                subs[added] = sub;
                inds[added] = ind;
                added++;
            }
        }

        for (int i = 0; i < sampleSize; i++) {
            Individual ind = state.population.subpops[subs[i]].individuals[inds[i]];
            PersistentSolution c = SolutionPersistence.createPersistentController(state, ind, subs[i], inds[i]);
            try {
                SolutionPersistence.writeSolutionToTar(c, taos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        try {
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(SampleSolutionsStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
