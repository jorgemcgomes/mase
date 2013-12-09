/*
 * To change this template, choose Tools | Templates
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
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestSolutionGenStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    public static final String P_KEEP_LAST = "keep-last";
    public static final String P_COMPRESS = "compress";
    public static final String LAST = "last.ind";
    protected File outFile;
    protected File last;
    protected TarArchiveOutputStream taos;
    protected boolean compress;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
        outFile = new File(outFile.getParent(), jobPrefix + outFile.getName());
        compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, true);
        if (compress) {
            try {
                taos = new TarArchiveOutputStream(
                        new GZIPOutputStream(
                                new BufferedOutputStream(new FileOutputStream(outFile))));
            } catch (IOException ex) {
                Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!compress && !outFile.exists()) {
            outFile.mkdirs();
        }
        if (state.parameters.getBoolean(base.push(P_KEEP_LAST), null, true)) {
            last = new File(outFile.getParent(), jobPrefix + LAST);
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
                float fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > bestFitness) {
                    bestFitness = fit;
                    best = ind;
                    sub = i;
                    index = j;
                }
            }
        }
        PersistentController c = SolutionPersistence.createPersistentController(best, state.generation, sub, index);
        try {
            if (compress) {
                SolutionPersistence.writeSolutionToTar(c, taos);
            } else {
                SolutionPersistence.writeSolutionInFolder(c, outFile);
            }
            if (last != null) {
                SolutionPersistence.writeSolution(c, last);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        if (compress) {
            try {
                taos.close();
            } catch (IOException ex) {
                Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
