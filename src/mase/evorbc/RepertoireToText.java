/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jorge
 */
public class RepertoireToText {

    public static final String FOLDER = "-f";

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(FOLDER)) {
                folders.add(new File(args[1 + i++]));
            }
        }

        for (File f : folders) {
            Collection<File> files = FileUtils.listFiles(f, new String[]{"collection.tar.gz"}, false);
            for (File file : files) {
                File out = new File(file.getAbsolutePath().replace("collection.tar.gz", "collection.stat"));
                System.out.println(file + " --> " + out);
                toText(file, out);
            }
        }

    }

    public static void toText(File in, File out) throws Exception {
        List<PersistentSolution> solutions = SolutionPersistence.readSolutionsFromTar(in);
        FileWriter fw = new FileWriter(out);
        boolean headed = false;
        for (PersistentSolution sol : solutions) {
            EvaluationResult[] evalResults = sol.getEvalResults();
            String behav = "";
            for (EvaluationResult e : evalResults) {
                if (e instanceof BehaviourResult) {
                    behav = e.toString();
                    break;
                }
            }

            if (!headed) { // add file header
                fw.write("Index OriginGeneration Fitness");
                for (int i = 0; i < behav.split(" ").length; i++) {
                    fw.write(" Behav_" + i);
                }
                fw.write("\n");
                headed = true;
            }

            fw.write(sol.getIndex() + " " + sol.getGeneration() + " " + sol.getFitness() + " " + behav + "\n");
        }
        fw.close();
    }

}
