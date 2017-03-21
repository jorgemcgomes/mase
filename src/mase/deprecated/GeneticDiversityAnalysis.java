/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.controllers.NeuralAgentController;
import mase.controllers.NeuralControllerIndividual;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class GeneticDiversityAnalysis {

    public static final String FOLDER = "-f";
    public static final String BLOCK = "-block";

    public static void main(String[] args) throws Exception {
        int block = 1;
        List<File> folders = new ArrayList<File>();
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(BLOCK)) {
                block = Integer.parseInt(args[1 + x++]);
            }
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        for (File folder : folders) {
            File[] files = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("sample.tar.gz");
                }
            });
            for (File file : files) {
                System.out.println(file.getAbsolutePath());
                analyse(file, block, 3);
            }
        }
    }

    private static void analyse(File f, int blockSize, int numAgents) throws Exception {
        List<PersistentSolution> solutions = SolutionPersistence.readSolutionsFromTar(f);

        int blockBegin = 0;
        ArrayList<double[]>[] buffer = new ArrayList[numAgents];
        for (int i = 0; i < numAgents; i++) {
            buffer[i] = new ArrayList<double[]>();
        }
        for (PersistentSolution sol : solutions) {
            if (sol.getGeneration() >= blockBegin + blockSize) {
                System.out.print(blockBegin);
                for (int i = 0; i < numAgents; i++) {
                    double d = computeDispersion(buffer[i]);
                    buffer[i].clear();
                    System.out.print(" " + d);
                }
                System.out.println();
                blockBegin = sol.getGeneration();
            }

            NeuralAgentController nac = (NeuralAgentController) sol.getController().getAgentControllers(numAgents)[sol.getSubpop()];
            buffer[sol.getSubpop()].add(nac.getNetwork().getFlat().getWeights());
        }

        if (!buffer[0].isEmpty()) {
            System.out.print(blockBegin);
            for (int i = 0; i < numAgents; i++) {
                double d = computeDispersion(buffer[i]);
                buffer[i].clear();
                System.out.print(" " + d);
            }
            System.out.println();
        }
    }

    private static double computeDispersion(ArrayList<double[]> vecs) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < vecs.size(); i++) {
            for (int j = i + 1; j < vecs.size(); j++) {
                sum += dist(vecs.get(i), vecs.get(j));
                count++;
            }
        }
        return sum / count;
    }

    private static double dist(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += FastMath.pow2(Math.abs(a[i] - b[i]));
        }
        return FastMath.sqrt(sum);
    }

}
