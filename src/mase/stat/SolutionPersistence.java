/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import com.thoughtworks.xstream.XStream;
import ec.EvolutionState;
import ec.Individual;
import ec.eval.MasterProblem;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import mase.MaseProblem;
import mase.controllers.GroupController;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SolutionPersistence {

    public static final XStream XSTREAM = new XStream();

    public static PersistentSolution createPersistentController(EvolutionState state, Individual ind, int sub, int index) {
        MaseProblem sp = (MaseProblem) (state.evaluator.p_problem instanceof MasterProblem
                ? ((MasterProblem) state.evaluator.p_problem).problem
                : state.evaluator.p_problem);
        PersistentSolution pc = new PersistentSolution();
        GroupController gc = ind.fitness.getContext() == null
                ? sp.getControllerFactory().createController(state, ind)
                : sp.getControllerFactory().createController(state, ind.fitness.getContext());        
        pc.setController(gc);
        
        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        pc.setEvalResults(fit.getEvaluationResults());
        pc.setOrigin(state.generation, sub, index);
        pc.setFitness(fit.getFitnessScore());
        return pc;
    }

    public static String autoFileName(PersistentSolution c) {
        return String.format("%03d", c.getGeneration()) + "_"
                + String.format("%02d", c.getSubpop()) + "_"
                + String.format("%03d", c.getIndex()) + "_"
                + String.format(Locale.ENGLISH, "%.2f", c.getFitness()) + ".xml";
    }
    
    public static void writeSolutionInFolder(PersistentSolution c, File outFolder) throws IOException {
        File out = new File(outFolder, autoFileName(c));
        writeSolution(c, out);
    }

    public static void writeSolution(PersistentSolution c, File output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        XSTREAM.toXML(c, fos);
        fos.close();
    }

    public static void writeSolutionToTar(PersistentSolution c, TarArchiveOutputStream out) throws IOException {
        String fileName = autoFileName(c);
        String xml = XSTREAM.toXML(c);
        byte[] ser = xml.getBytes();
        TarArchiveEntry e = new TarArchiveEntry(fileName);
        e.setSize(ser.length);
        out.putArchiveEntry(e);
        out.write(ser, 0, ser.length);
        out.closeArchiveEntry();
        out.flush();
    }

    public static PersistentSolution readSolution(InputStream is) throws Exception {
        try {
            return (PersistentSolution) XSTREAM.fromXML(is);
        } catch (Exception ex) { 
            ex.printStackTrace();
            return null;
        }
    }

    public static PersistentSolution readSolutionFromFile(File f) throws Exception {
        PersistentSolution sol = (PersistentSolution) XSTREAM.fromXML(f);
        return sol;
    }

    public static List<PersistentSolution> readSolutionsFromTar(File tarFile) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(tarFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gis);
        ArrayList<PersistentSolution> gcs = new ArrayList<>();
        TarArchiveEntry e;
        while ((e = tis.getNextTarEntry()) != null) {
            PersistentSolution gc = readSolution(tis);
            gcs.add(gc);
        }
        gis.close();
        tis.close();

        Collections.sort(gcs);
        return gcs;
    }

    public static TarArchiveOutputStream reopenTar(File file) {
        try {
            File temp = File.createTempFile("tararchive", ".gz");
            FileUtils.copyFile(file, temp);
            TarArchiveInputStream tais = new TarArchiveInputStream(
                    new GZIPInputStream(
                            new BufferedInputStream(new FileInputStream(temp))));
            file.delete();
            TarArchiveOutputStream taos = new TarArchiveOutputStream(
                    new GZIPOutputStream(
                            new BufferedOutputStream(new FileOutputStream(file))));

            ArchiveEntry nextEntry;
            while ((nextEntry = tais.getNextEntry()) != null) {
                taos.putArchiveEntry(nextEntry);
                IOUtils.copy(tais, taos, (int) nextEntry.getSize());
                taos.closeArchiveEntry();
            }
            return taos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
