/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonReevaluate.Reevaluation;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;

/**
 *
 * @author jorge
 */
public class MasterReevaluate {

    public static final String FOLDER = "-f";

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<File>();
        int reps = 0;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(MasonReevaluate.P_NREPS)) {
                reps = Integer.parseInt(args[1 + x++]);
            }
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        MasonSimulator sim = MasonPlayer.createSimulator(args);
        sim.repetitions = 1;
        MasterReevaluate mt = new MasterReevaluate(sim, reps);
        for (File f : folders) {
            mt.reevaluateFolder(f);
        }
        mt.shutdown();
    }
    private final MasonSimulator sim;
    private final ExecutorService executor;
    private final int reps;

    public MasterReevaluate(MasonSimulator sim, int reps) {
        this.sim = sim;
        this.reps = reps;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void reevaluateFolder(File folder) throws Exception {
        System.out.println(folder.getAbsolutePath());
        // Find all the relevant tars under the given folders
        List<File> tars = new ArrayList<File>();
        int job = 0;
        while (true) {
            File b0 = new File(folder, "job." + job + ".bests.tar.gz");
            if (b0.exists()) {
                tars.add(b0);
                job++;
            } else {
                break;
            }
        }

        for (File tar : tars) {
            System.out.println("\n" + tar.getAbsolutePath());
            // IO
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(tar);
            File fitnessLog = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", "refitness.stat"));
            File behavLog = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", "rebehaviours.stat"));
            BufferedWriter fitWriter = new BufferedWriter(new FileWriter(fitnessLog));
            BufferedWriter behavWriter = new BufferedWriter(new FileWriter(behavLog));

            // Reevaluate solutions
            List<Worker> workers = new ArrayList<Worker>(sols.size());
            for (int i = 0; i < sols.size(); i++) {
                workers.add(new Worker(sols.get(i)));
            }
            List<Future<Reevaluation>> results = executor.invokeAll(workers);

            // Log results
            double bestFar = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;
            EvaluationResult[] bestEval = null;
            for (int i = 0; i < sols.size(); i++) {
                Reevaluation reev = results.get(i).get();
                // Log fitness
                if (reev.meanFitness > bestFar) {
                    bestFar = reev.meanFitness;
                    bestEval = reev.mergedResults;
                    bestIndex = i;
                }
                fitWriter.write(i + " 0 " + reev.meanFitness + " " + bestFar + " 0 " + reev.meanFitness + " " + bestFar);
                fitWriter.newLine();

                // Log behaviours
                behavWriter.write(i + " " + sols.get(i).getSubpop() + " " + sols.get(i).getIndex() + " " + reev.meanFitness);
                for (int j = 1; j < reev.mergedResults.length; j++) { // starts at 1 to skip fitness
                    EvaluationResult br = reev.mergedResults[j];
                    if(br instanceof SubpopEvaluationResult) {
                        SubpopEvaluationResult ser = (SubpopEvaluationResult) br;
                        br = ser.getSubpopEvaluation(sols.get(i).getSubpop());
                    }
                    behavWriter.write(" " + br.toString());
                }
                behavWriter.newLine();
            }
            PersistentSolution best = sols.get(bestIndex);
            best.setEvalResults(bestEval);
            best.setFitness((float) bestFar);
            File bestFile = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", "rebest.ind"));
            SolutionPersistence.writeSolution(best, bestFile);

            fitWriter.close();
            behavWriter.close();
        }
    }

    protected void shutdown() {
        executor.shutdown();
    }

    private class Worker implements Callable<Reevaluation> {

        private final PersistentSolution sol;

        public Worker(PersistentSolution sol) {
            this.sol = sol;
        }

        @Override
        public Reevaluation call() throws Exception {
            System.out.print(".");
            Reevaluation reev = MasonReevaluate.reevaluate(sol, sim, reps);
            return reev;
        }
    }
}
