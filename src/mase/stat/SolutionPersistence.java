/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import mase.SimulationProblem;
import mase.controllers.GroupController;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SolutionPersistence {

    public static PersistentSolution createPersistentController(EvolutionState state, Individual ind, int sub, int index) {
        SimulationProblem sp = (SimulationProblem) state.evaluator.p_problem;
        PersistentSolution pc = new PersistentSolution();
        GroupController gc = ind.fitness.getContext() == null ? 
                sp.createController(state, ind) : 
                sp.createController(state, ind.fitness.getContext());
        pc.setController(gc);

        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        pc.setEvalResults(fit.getEvaluationResults());
        pc.setOrigin(state.generation, sub, index);
        pc.setFitness(fit.getFitnessScore());
        return pc;
    }

    public static void writeSolutionInFolder(PersistentSolution c, File outFolder) throws IOException {
        File out = new File(outFolder, String.format("%03d", c.getGeneration()) + "_"
                + String.format("%02d", c.getSubpop()) + "_"
                + String.format("%03d", c.getIndex()) + "_"
                + String.format("%.2f", c.getFitness()) + ".ind");
        writeSolution(c, out);
    }

    public static void writeSolution(PersistentSolution c, File output) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
        oos.writeObject(c);
        oos.flush();
        oos.close();
    }

    public static void writeSolutionToTar(PersistentSolution c, TarArchiveOutputStream out) throws IOException {
        String fileName = String.format("%03d", c.getGeneration()) + "_"
                + String.format("%02d", c.getSubpop()) + "_"
                + String.format("%03d", c.getIndex()) + "_"
                + String.format("%.2f", c.getFitness()) + ".ind";

        byte[] ser = SerializationUtils.serialize(c);
        TarArchiveEntry e = new TarArchiveEntry(fileName);
        e.setSize(ser.length);
        out.putArchiveEntry(e);
        out.write(ser, 0, ser.length);

        /*File temp = File.createTempFile("solution", ".ind");
         writeSolution(c, temp);
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(temp));
         IOUtils.copy(bis, out);*/
        out.closeArchiveEntry();
        out.flush();
    }

    public static PersistentSolution readSolution(InputStream is) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(is);
        PersistentSolution controller = (PersistentSolution) ois.readObject();
        return controller;
    }
    
    public static List<PersistentSolution> readSolutionsFromTar(File tarFile) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(tarFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gis);

        ArrayList<PersistentSolution> gcs = new ArrayList<PersistentSolution>();        
        TarArchiveEntry e;
        while ((e = tis.getNextTarEntry()) != null) {
            PersistentSolution gc = readSolution(tis);
            gcs.add(gc);
        }
        tis.close();

        Collections.sort(gcs);
        return gcs;
    }
}
