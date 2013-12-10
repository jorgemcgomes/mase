/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonReevaluate.Reevaluation;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;

/**
 * Input: .tar.gz file Output: CSV with all the individuals re-evaluated
 *
 * @author jorge
 */
public class BatchReevaluate {

    public static final String TAR = "-tar";

    public static void main(String[] args) throws IOException, Exception {
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

        // Reevaluation stats initialization
        File logFile = new File(tar.getParentFile(), tar.getName().replace("tar.gz", "re.stat"));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(logFile));
        double bestSoFar = Double.NEGATIVE_INFINITY;

        // Reevaluate
        MasonSimulator sim = MasonPlayer.createSimulator(args);
        List<PersistentSolution> gcs = SolutionPersistence.readSolutionsFromTar(tar);
        PersistentSolution best = null;
        for (PersistentSolution gc : gcs) {
            Reevaluation re = MasonReevaluate.reevaluate(gc, sim, nreps);
            if (re.meanFitness > bestSoFar) {
                bestSoFar = re.meanFitness;
                best = gc;
            }
            bfw.write(gc.getGeneration() + " " + gc.getSubpop() + " " + gc.getIndex() + " " + re.meanFitness + " " + re.sdFitness + " " + bestSoFar);
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
        }

        System.out.println("All time best\n: " + best);
        bfw.close();
    }
}
