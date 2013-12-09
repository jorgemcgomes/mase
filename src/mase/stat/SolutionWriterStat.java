/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
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
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.GroupController;
import mase.evaluation.ExpandedFitness;
import mase.controllers.HeterogeneousGroupController;
import mase.controllers.HomogeneousGroupController;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SolutionWriterStat extends Statistics {

    protected String jobPrefix;
    protected EvolutionState state;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.state = state;
        if (state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1) > 1) {
            int jobN = (Integer) state.job[0];
            jobPrefix = "job." + jobN + ".";
        } else {
            jobPrefix = "";
        }
    }

    public static PersistentController createPersistentController(Individual ind, int gen, int sub, int index) {
        PersistentController pc = new PersistentController();
        GroupController gc = null;
        if (ind.fitness.getContext() == null) {
            gc = new HomogeneousGroupController(((AgentControllerIndividual) ind).decodeController());
        } else {
            Individual[] inds = ind.fitness.getContext();
            AgentController[] acs = new AgentController[inds.length];
            for (int i = 0; i < inds.length; i++) {
                acs[i] = ((AgentControllerIndividual) inds[i]).decodeController();
            }
            gc = new HeterogeneousGroupController(acs);
        }
        pc.setController(gc);

        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        pc.setEvalResults(fit.getEvaluationResults());
        pc.setOrigin(gen, sub, index);
        pc.setFitness(fit.getFitnessScore());
        return pc;
    }

    public static void writeSolutionInFolder(PersistentController c, File outFolder) throws IOException {
        File out = new File(outFolder, String.format("%03d", c.getGeneration()) + "_"
                + String.format("%02d", c.getSubpop()) + "_"
                + String.format("%03d", c.getIndex()) + "_"
                + String.format("%.2f", c.getFitness()) + ".ind");
        writeSolution(c, out);
    }

    public static void writeSolution(PersistentController c, File output) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
        oos.writeObject(c);
        oos.flush();
        oos.close();
    }

    public static void writeSolutionToTar(PersistentController c, TarArchiveOutputStream out) throws IOException {
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
    }

    public static PersistentController readSolution(InputStream is) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(is);
        PersistentController controller = (PersistentController) ois.readObject();
        return controller;
    }
    
    public static List<PersistentController> readSolutionsFromTar(File tarFile) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(tarFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gis);

        ArrayList<PersistentController> gcs = new ArrayList<PersistentController>();        
        TarArchiveEntry e;
        while ((e = tis.getNextTarEntry()) != null) {
            PersistentController gc = readSolution(tis);
            gcs.add(gc);
        }
        tis.close();

        Collections.sort(gcs);
        return gcs;
    }
}
