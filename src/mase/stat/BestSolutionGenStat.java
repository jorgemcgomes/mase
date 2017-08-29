/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.stat.StatUtils.IndividualInfo;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestSolutionGenStat extends Statistics {

    public static final String P_FILE = "file";
    public static final String P_KEEP_LAST = "keep-last";
    public static final String P_DO_SUBPOPS = "do-subpops";
    public static final String P_FILE_LAST = "file-last";
    private static final long serialVersionUID = 1L;
    protected File archiveFile;
    protected File lastBaseFile;
    protected File[] lastFile;
    protected transient TarArchiveOutputStream taos;
    protected boolean doSubpops;
    protected boolean keepLast;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        archiveFile = state.parameters.getFile(base.push(P_FILE), null);
        try {
            int log = state.output.addLog(archiveFile, false);
            archiveFile = state.output.getLog(log).filename;
            taos = new TarArchiveOutputStream(
                    new GZIPOutputStream(
                            new BufferedOutputStream(new FileOutputStream(archiveFile))));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        lastBaseFile = state.parameters.getFile(base.push(P_FILE_LAST), null);
        
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS), null, false);
        keepLast = state.parameters.getBoolean(base.push(P_KEEP_LAST), null, true);
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        try {
        if (keepLast) {
            if (doSubpops) {
                lastFile = new File[state.population.subpops.size()];
                for (int i = 0; i < state.population.subpops.size(); i++) {
                    File f = new File(FilenameUtils.getBaseName(lastBaseFile.getName()) + "_" + i + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(lastBaseFile.getName()));
                    int l = state.output.addLog(f, false);
                    lastFile[i] = state.output.getLog(l).filename;
                }
            } else {
                int l = state.output.addLog(lastBaseFile, false);
                lastFile = new File[]{state.output.getLog(l).filename};
            }
        }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        if (taos == null) { // can happen in case of resuming from checkpoint
            taos = SolutionPersistence.reopenTar(archiveFile);
        }

        if (doSubpops) {
            IndividualInfo[] subpopBests = StatUtils.getSubpopBests(state);
            for (IndividualInfo ind : subpopBests) {
                logInd(state, ind);
            }
        } else {
            IndividualInfo best = StatUtils.getBest(state);
            logInd(state, best);
        }
    }

    private void logInd(EvolutionState state, IndividualInfo ind) {
        PersistentSolution p = SolutionPersistence.createPersistentController(state, ind.ind, ind.sub, ind.index);
        if (keepLast) {
            try {
                SolutionPersistence.writeSolution(p, doSubpops ? lastFile[ind.sub] : lastFile[0]);
            } catch (IOException ex) {
                Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            SolutionPersistence.writeSolutionToTar(p, taos);
        } catch (IOException ex) {
            Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        try {
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(BestSolutionGenStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
