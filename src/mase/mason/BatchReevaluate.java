/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import mase.EvaluationResult;
import mase.GroupController;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonReevaluate.Reevaluation;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Input: .tar.gz file Output: CSV with all the individuals re-evaluated
 *
 * @author jorge
 */
public class BatchReevaluate {

    public static final String TAR = "-tar";

    public static void main(String[] args) throws IOException {
        DecimalFormat df = new DecimalFormat("0.000");        
        // Parameter loading
        File tar = null;
        int x;
        int nreps = 0;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(TAR)) {
                tar = new File(args[1 + x++]);
            } else if (args[x].equals(MasonReevaluate.P_NREPS)) {
                nreps = Integer.parseInt(args[1 + x++]);
            }
        }
        if (nreps <= 0 || tar == null || !tar.exists()) {
            System.out.println("Wrong or missing arguments.");
            System.exit(1);
        }

        // Extract files from tar.gz
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(tar));
        TarArchiveInputStream tis = new TarArchiveInputStream(gis);
        TarArchiveEntry e;
        LinkedList<File> fileList = new LinkedList<File>();
        File extractDir = File.createTempFile(tar.getName(), "");
        extractDir.delete();
        extractDir.mkdir();
        while ((e = tis.getNextTarEntry()) != null) {
            File temp = new File(extractDir, e.getName());
            FileOutputStream ofs = new FileOutputStream(temp);
            IOUtils.copy(tis, ofs);
            ofs.close();
            fileList.add(temp);
        }
        Collections.sort(fileList);

        // Reevaluate
        MasonSimulator sim = MasonPlayer.createSimulator(args);
        File logFile = new File(tar.getParentFile(), tar.getName().replace("tar.gz", "re.stat"));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(logFile));
        double bestSoFar = Double.NEGATIVE_INFINITY;
        File best = null;

        for (File f : fileList) {
            GroupController gc = MasonPlayer.loadController(f, false);
            Reevaluation re = MasonReevaluate.reevaluate(gc, sim, nreps);
            if (re.meanFitness > bestSoFar) {
                bestSoFar = re.meanFitness;
                best = f;
            }
            String[] split = f.getName().split("_");
            bfw.write(split[0] + " " + split[1] + " " + split[2] + " " + re.meanFitness + " " + re.sdFitness + " " + bestSoFar);
            for (EvaluationResult er : re.mergedResults) {
                if (er instanceof SubpopEvaluationResult) {
                    SubpopEvaluationResult aer = (SubpopEvaluationResult) er;
                    for (EvaluationResult er2 : aer.getAllEvaluations()) {
                        bfw.write(" " + er2);
                    }
                } else {
                    bfw.write(" " + er);
                }
            }
            bfw.newLine();
            System.out.println("Gen:" + split[0] + " " + split[1] + " " + 
                    split[2] + " Mean: " + df.format(re.meanFitness) + " SD: " + 
                    df.format(re.sdFitness) + " Best: " + df.format(bestSoFar));
        }
        System.out.println("All time best: " + best.getName());

        bfw.close();
        FileUtils.deleteDirectory(extractDir);
    }
}
