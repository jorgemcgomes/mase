/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 *
 * @author Jorge
 */
public class MEFinalRepertoireStat extends Statistics {

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
        try {
            int l = state.output.addLog(archiveFile, false);
            taos = new TarArchiveOutputStream(new GZIPOutputStream(
                    new BufferedOutputStream(new FileOutputStream(state.output.getLog(l).filename))));
            
            MESubpopulation sub = (MESubpopulation) state.population.subpops.get(0);
            Collection<Entry<Integer, Individual>> entries = sub.map.entries();
            for (Entry<Integer, Individual> e : entries) {
                PersistentSolution p = SolutionPersistence.createPersistentController(state, e.getValue(), 0, e.getKey());
                p.setUserData(sub.getBehaviourVector(state, e.getValue()));
                SolutionPersistence.writeSolutionToTar(p, taos);
            }
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(MEFinalRepertoireStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
