/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

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
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 *
 * @author Jorge
 */
public class FinalArchiveStat extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    protected File archiveFile;
    protected transient TarArchiveOutputStream taos;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        archiveFile = state.parameters.getFile(base.push(P_FILE), null);
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);

        NoveltyEvaluation ne = null;
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof NoveltyEvaluation) {
                ne = (NoveltyEvaluation) pe;
                break;
            }
        }

        try {
            int l = state.output.addLog(archiveFile, false);
            taos = new TarArchiveOutputStream(new GZIPOutputStream(
                    new BufferedOutputStream(new FileOutputStream(state.output.getLog(l).filename))));
            
            for (int a = 0; a < ne.archives.length; a++) {
                for (int x = 0; x < ne.archives[a].size(); x++) {
                    ArchiveEntry e = ne.archives[a].get(x);
                    PersistentSolution p = SolutionPersistence.createPersistentController(state, e.getIndividual(), a, x);
                    SolutionPersistence.writeSolutionToTar(p, taos);
                }
            }
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(FinalArchiveStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
