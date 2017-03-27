/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mase.MaseProblem;
import mase.evaluation.EvaluationResult;
import mase.stat.Reevaluate.Reevaluation;

/**
 *
 * @author jorge
 */
public class BatchReevaluate {

    public static final String FOLDER = "-f";
    public static final String FORCE = "-force";
    public static final String RECURSIVE = "-recursive";
    public static final String PREFIX = "-prefix";
    public static final String ALL_SUBPOPS = "-allsubs";
    public static final String DEFAULT_PREFIX = "post";

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<>();
        int reps = 0;
        boolean recursive = false;
        boolean force = false;
        boolean allSubpops = false;
        String prefix = DEFAULT_PREFIX;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(Reevaluate.P_NREPS)) {
                reps = Integer.parseInt(args[1 + x++]);
            } else if (args[x].equalsIgnoreCase(FORCE)) {
                force = true;
            } else if (args[x].equalsIgnoreCase(RECURSIVE)) {
                recursive = true;
            } else if (args[x].equalsIgnoreCase(PREFIX)) {
                prefix = args[1 + x++];
            } else if (args[x].equalsIgnoreCase(ALL_SUBPOPS)) {
                allSubpops = true;
            }
        }
        if (reps <= 0) {
            System.out.println("Invalid number of repetitions: " + reps);
            return;
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        BatchReevaluate mt = new BatchReevaluate(reps, allSubpops, force, prefix);

        for (File f : folders) {
            try {
                if (f.isDirectory()) {
                    processDir(f, args, mt, recursive);
                } else if (f.getName().endsWith("tar.gz")) {
                    MaseProblem sim = Reevaluate.createSimulator(args, f.getParentFile());
                    mt.reevaluateTar(f, sim);
                } else {
                    System.out.println("Cannot handle file: " + f.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mt.shutdown();
    }

    private static void processDir(File f, String[] args, BatchReevaluate mt, boolean recursive) throws Exception {
        System.out.println(f.getAbsolutePath());

        // Reevaluate this folder if there are any files here
        File[] list = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.startsWith("job.");
            }
        });
        if (!recursive || list.length > 0) {
            MaseProblem sim = Reevaluate.createSimulator(args, f);
            mt.reevaluateFolder(f, sim);
        }

        // Recursive step
        if (recursive) {
            File[] subdirs = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File current) {
                    return current.isDirectory();
                }
            });
            for (File sub : subdirs) {
                processDir(sub, args, mt, recursive);
            }
        }
    }

    private final int reps;
    private final boolean force;
    private final String prefix;
    private final ExecutorService executor;
    private final boolean allSubpops;

    public BatchReevaluate(int reps, boolean allSubpops, boolean force, String prefix) {
        this.reps = reps;
        this.allSubpops = allSubpops;
        this.force = force;
        this.prefix = prefix;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void reevaluateFolder(File folder, MaseProblem sim) throws Exception {
        System.out.println(folder.getAbsolutePath());
        // Find all the relevant tars under the given folders
        List<File> tars = new ArrayList<>();
        for (int job = 0; job < 100; job++) {
            File b0 = new File(folder, "job." + job + ".bests.tar.gz");
            if (b0.exists()) {
                File re = new File(folder, "job." + job + "." + prefix + "fitness.stat");
                if (re.exists() && !force) {
                    System.out.println("Skipping " + b0.getAbsolutePath());
                } else {
                    tars.add(b0);
                }
            }
        }
        for (File tar : tars) {
            reevaluateTar(tar, sim);
        }
    }

    protected void reevaluateTar(File tar, MaseProblem sim) throws Exception {
        System.out.println("\n" + tar.getAbsolutePath());
        // Output files
        File fitnessLog = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", prefix + "fitness.stat"));
        File behavLog = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", prefix + "behaviours.stat"));
        File bestFile = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", prefix + "best.xml"));

        BufferedWriter fitWriter = new BufferedWriter(new FileWriter(fitnessLog));
        fitWriter.write("Generation Evaluations Subpop Individuals MinFitness MeanFitness MaxFitness BestSoFar");
        fitWriter.newLine();

        BufferedWriter behavWriter = new BufferedWriter(new FileWriter(behavLog));

        try {
            // Read solutions
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(tar);

            // Reevaluate solutions
            List<Worker> workers = new ArrayList<>(sols.size());
            for (PersistentSolution sol : sols) {
                workers.add(new Worker(sol, sim));
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
                fitWriter.write(i + " 0 NA 0 0 0 " + reev.meanFitness + " " + bestFar);
                fitWriter.newLine();

                // file header
                if (i == 0) {
                    behavWriter.write(EvaluationsStat.header(sim, reev.mergedResults, allSubpops));
                    behavWriter.newLine();
                }

                // Log behaviours
                behavWriter.write(EvaluationsStat.entry(sols.get(i).getGeneration(), 
                        sols.get(i).getSubpop(), sols.get(i).getIndex(), reev.mergedResults, allSubpops));
                behavWriter.newLine();
            }
            PersistentSolution best = sols.get(bestIndex);
            best.setEvalResults(bestEval);
            best.setFitness(bestFar);
            SolutionPersistence.writeSolution(best, bestFile);
            fitWriter.close();
            behavWriter.close();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage() + "\nDeleting all the logs...");
            fitWriter.close();
            behavWriter.close();
            fitnessLog.delete();
            behavLog.delete();
            bestFile.delete();
            ex.printStackTrace();
        }
    }

    protected void shutdown() {
        executor.shutdown();
    }

    private class Worker implements Callable<Reevaluation> {

        private final PersistentSolution sol;
        private final MaseProblem sim;

        public Worker(PersistentSolution sol, MaseProblem sim) {
            this.sol = sol;
            this.sim = sim;
        }

        @Override
        public Reevaluation call() throws Exception {
            System.out.print(".");
            Reevaluation reev = Reevaluate.reevaluate(sol, sim, reps);
            return reev;
        }
    }
}
