/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.ControllerDecoder;
import mase.GroupController;
import mase.SimulationProblem;
import mase.ExpandedFitness;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SolutionWriterStat extends Statistics {

    protected String prefix;
    protected EvolutionState state;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.state = state;
        if (state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1) > 1) {
            int jobN = (Integer) state.job[0];
            prefix = "job." + jobN + ".";
        } else {
            prefix = "";
        }
    }

    protected void writeSolution(Individual ind, File out) {
        ControllerDecoder decoder = ((SimulationProblem) state.evaluator.p_problem).getControllerDecoder();
        GroupController controller = decoder.decodeController(ind.fitness.getContext());
        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out));
            oos.writeObject(controller);
            oos.writeObject(fit.evaluations());
            oos.flush();
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(SolutionWriterStat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void compressFolder(File inFolder, boolean deleteAfter) {
        File out = new File(inFolder.getParent(), inFolder.getName() + ".tar.gz");
        compressFolder(inFolder, out, deleteAfter);
    }

    public static void compressFolder(File inFolder, File outFile, boolean deleteAfter) {
        TarArchiveOutputStream tOut;
        try {
            tOut = new TarArchiveOutputStream(
                    new GZIPOutputStream(
                    new BufferedOutputStream(new FileOutputStream(outFile))));
            File[] files = inFolder.listFiles();
            for (File f : files) {
                addFilesToCompression(tOut, f, ".");
            }
            tOut.finish();
            tOut.close();
        } catch (Exception ex) {
            Logger.getLogger(SolutionWriterStat.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (deleteAfter) {
            try {
                FileUtils.deleteDirectory(inFolder);
            } catch (IOException ex) {
                Logger.getLogger(SolutionWriterStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir)
            throws IOException {
        // Create an entry for the file
        taos.putArchiveEntry(new TarArchiveEntry(file, dir + File.separator + file.getName()));
        if (file.isFile()) {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, taos);
            taos.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive
            for (File childFile : file.listFiles()) {
                addFilesToCompression(taos, childFile, file.getName());
            }
        }
    }
}
