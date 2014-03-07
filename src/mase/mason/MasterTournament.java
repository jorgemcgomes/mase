/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mase.controllers.AgentController;
import mase.controllers.HeterogeneousGroupController;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author jorge
 */
public class MasterTournament {

    public static final String TEST_FOLDER = "-t";
    public static final String SAMPLE_FOLDER = "-s";
    public static final String BOTH_FOLDER = "-ts";
    public static final String FREQUENCY = "-freq";
    public static final String OUTNAME = "-name";
    public static final String SELF = "-self";
    public static final String ELITE = "-elite";
    public static final String INDIVIDUALS = "-inds";

    public static void main(String[] args) throws Exception {
        List<File> sampleFolders = new ArrayList<File>();
        List<File> testFolders = new ArrayList<File>();
        int freq = 0;
        String name = "";
        boolean self = false;
        String individuals = null;
        int elite = 0;

        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(TEST_FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                testFolders.add(folder);
            } else if(args[x].equalsIgnoreCase(SAMPLE_FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                sampleFolders.add(folder); 
            } else if(args[x].equalsIgnoreCase(BOTH_FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                sampleFolders.add(folder); 
                testFolders.add(folder);
            } else if (args[x].equalsIgnoreCase(FREQUENCY)) {
                freq = Integer.parseInt(args[1 + x++]);
            } else if (args[x].equalsIgnoreCase(ELITE)) {
                elite = Integer.parseInt(args[1 + x++]);
            } else if (args[x].equalsIgnoreCase(OUTNAME)) {
                name = args[1 + x++];
            } else if (args[x].equalsIgnoreCase(SELF)) {
                self = true;
            } else if (args[x].equalsIgnoreCase(INDIVIDUALS)) {
                individuals = args[1 + x++];
            }
        }

        if (testFolders.isEmpty() || sampleFolders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        MasonSimulator sim = MasonPlayer.createSimulator(args);
        MasterTournament mt = new MasterTournament(sampleFolders, testFolders, sim, name);

        if (individuals != null) {
            mt.makeIndsTournaments(individuals);
        } else if (self) {
            mt.makeSelfTournaments(freq);
        } else {
            mt.makeSampleTournaments(freq, elite);
        }

        mt.executor.shutdown();
    }
    private final List<File> sampleFolders;
    private final List<File> testFolders;
    private final MasonSimulator sim;
    private final ExecutorService executor;
    private final String name;

    public MasterTournament(List<File> sampleFolders, List<File> testFolders, MasonSimulator sim, String name) {
        this.sampleFolders = sampleFolders;
        this.testFolders = testFolders;
        this.sim = sim;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.name = name;
    }

    private List<File>[] findTars(List<File> folders) {
        // Find all the relevant tars under the given folders
        List<File>[] tars = new List[]{new ArrayList<File>(), new ArrayList<File>()};
        for (File folder : folders) {
            int i = 0;
            while (true) {
                File b0 = new File(folder, "job." + i + ".bests.0.tar.gz");
                if (b0.exists()) {
                    File b1 = new File(folder, "job." + i + ".bests.1.tar.gz");
                    tars[0].add(b0);
                    tars[1].add(b1);
                    i++;
                } else {
                    break;
                }
            }
        }
        return tars;
    }

    public void makeSelfTournaments(int freq) throws Exception {
        List<File>[] tars = findTars(testFolders);
        List<AgentController>[] samples = new List[2];

        // Make tournaments
        for (int job = 0; job < tars[0].size(); job++) {
            // Make evaluations -- test every best from every generation against the samples
            List<EvaluationResult[]>[] subpopEvals = new List[2];
            List<PersistentSolution>[] solutions = new List[2];
            File log = new File(tars[0].get(job).getAbsolutePath().replace("bests.0.tar.gz", ""));
            if(log.exists()) {
                System.out.println("Log already exists. Skipping. " + log.getAbsolutePath());
            } else {
                for (int s = 0; s < 2; s++) {
                    int opposing = s == 0 ? 1 : 0;
                    samples[s] = loadSample(Collections.singletonList(tars[opposing].get(job)), opposing, freq);
                    File tar = tars[s].get(job);
                    System.out.println(tar.getAbsolutePath());
                    solutions[s] = SolutionPersistence.readSolutionsFromTar(tar);
                    List<AgentController> all = loadControllers(solutions[s], s, 1);
                    System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + samples[s].size());
                    subpopEvals[s] = tournament(all, samples[s], s);
                }
                logResults(solutions, subpopEvals, log.getAbsolutePath());
            }
        }
    }

    public void makeSampleTournaments(int freq, int elite) throws Exception {
        // Load samples
        List<File>[] sampleTars = findTars(sampleFolders);
        List<AgentController>[] samples = new List[2];
        samples[0] = loadSample(sampleTars[1], 1, freq);
        samples[0].addAll(loadElite(sampleTars[1], 1, elite));
        samples[1] = loadSample(sampleTars[0], 0, freq);
        samples[1].addAll(loadElite(sampleTars[0], 0, elite));
        
        List<File>[] testTars = findTars(testFolders);

        // Make tournaments
        for (int job = 0; job < testTars[0].size(); job++) {
            // Make evaluations -- test every best from every generation against the samples
            List<EvaluationResult[]>[] subpopEvals = new List[2];
            List<PersistentSolution>[] solutions = new List[2];
            for (int s = 0; s < 2; s++) {
                File tar = testTars[s].get(job);
                System.out.println(tar.getAbsolutePath());
                solutions[s] = SolutionPersistence.readSolutionsFromTar(tar);
                List<AgentController> all = loadControllers(solutions[s], s, 1);
                System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + samples[s].size());
                subpopEvals[s] = tournament(all, samples[s], s);
            }

            logResults(solutions, subpopEvals, testTars[0].get(job).getAbsolutePath().replace("bests.0.tar.gz", ""));
        }
    }

    public void makeIndsTournaments(String indName) throws Exception {
        List<PersistentSolution> inds = new ArrayList<PersistentSolution>();
        for (File folder : sampleFolders) {
            Collection<File> files = FileUtils.listFiles(folder, new SuffixFileFilter(indName), TrueFileFilter.INSTANCE);
            for (File f : files) {
                FileInputStream fis = new FileInputStream(f);
                inds.add(SolutionPersistence.readSolution(fis));
                fis.close();
            }
        }

        List<AgentController>[] samples = new List[2];
        samples[0] = new ArrayList<AgentController>(inds.size());
        samples[1] = new ArrayList<AgentController>(inds.size());
        for (PersistentSolution sol : inds) {
            AgentController[] controllers = sol.getController().getAgentControllers(2);
            samples[0].add(controllers[1]);
            samples[1].add(controllers[0]);
        }

        List<File>[] tars = findTars(testFolders);
        // Make tournaments
        for (int job = 0; job < tars[0].size(); job++) {
            // Make evaluations -- test every best from every generation against the samples
            List<EvaluationResult[]>[] subpopEvals = new List[2];
            List<PersistentSolution>[] solutions = new List[2];
            for (int s = 0; s < 2; s++) {
                File tar = tars[s].get(job);
                System.out.println(tar.getAbsolutePath());
                solutions[s] = SolutionPersistence.readSolutionsFromTar(tar);
                List<AgentController> all = loadControllers(solutions[s], s, 1);
                System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + samples[s].size());
                subpopEvals[s] = tournament(all, samples[s], s);
            }

            logResults(solutions, subpopEvals, tars[0].get(job).getAbsolutePath().replace("bests.0.tar.gz", ""));
        }
    }

    private void logResults(List<PersistentSolution>[] solutions, List<EvaluationResult[]>[] subpopEvals, String outPath) throws Exception {
        // Log results
        File log = new File(outPath + "comp" + name + ".stat");
        BufferedWriter bfw = new BufferedWriter(new FileWriter(log));
        float[] bestFar = new float[]{Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (int g = 0; g < subpopEvals[0].size(); g++) {
            bfw.write(g + "");
            for (int s = 0; s < 2; s++) {
                EvaluationResult[] er = subpopEvals[s].get(g);
                // assumes fitness evaluation is in first index
                float fit = (Float) (((SubpopEvaluationResult) er[0]).getSubpopEvaluation(s).value());
                // assumes behaviour evaluation is in second index
                BehaviourResult br = null;
                if (er[1] instanceof SubpopEvaluationResult) {
                    br = (BehaviourResult) ((SubpopEvaluationResult) er[1]).getSubpopEvaluation(s);
                } else {
                    br = (BehaviourResult) er[1];
                }
                bestFar[s] = Math.max(bestFar[s], fit);
                bfw.write(" " + fit + " " + bestFar[s] + " " + br.toString());
            }
            bfw.newLine();
        }
        bfw.close();

        // Persist the most interesting challenge
        PersistentSolution best0 = solutions[0].get(bestIndex(subpopEvals[0], 0));
        PersistentSolution best1 = solutions[1].get(bestIndex(subpopEvals[1], 1));
        HeterogeneousGroupController hc0 = (HeterogeneousGroupController) best0.getController();
        HeterogeneousGroupController hc1 = (HeterogeneousGroupController) best1.getController();
        HeterogeneousGroupController newC = new HeterogeneousGroupController(new AgentController[]{
            hc0.getAgentControllers(2)[0],
            hc1.getAgentControllers(2)[1]
        });
        SubpopEvaluationResult ser = new SubpopEvaluationResult(
                new FitnessResult(bestFar[0]),
                new FitnessResult(bestFar[1]));
        PersistentSolution sol = new PersistentSolution();
        sol.setController(newC);
        sol.setEvalResults(new EvaluationResult[]{ser});
        File superBest = new File(outPath + "challenge" + name + ".ind");
        SolutionPersistence.writeSolution(sol, superBest);
    }

    private int bestIndex(List<EvaluationResult[]> evals, int subpop) {
        float best = Float.MIN_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < evals.size(); i++) {
            EvaluationResult[] e = evals.get(i);
            float fit = (Float) (((SubpopEvaluationResult) e[0]).getSubpopEvaluation(subpop).value());
            if (fit > best) {
                best = fit;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private List<AgentController> loadElite(List<File> tars, int subpop, int elite) throws Exception {
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        for (File f : tars) {
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(f);
            for (int i = 0; i < elite; i++) {
                list.add(getAC(sols.get(sols.size() - i - 1), subpop));
            }
        }
        return list;
    }

    private List<AgentController> loadSample(List<File> tars, int subpop, int sampleFreq) throws Exception {
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        for (File f : tars) {
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(f);
            List<AgentController> cs = loadControllers(sols, subpop, sampleFreq);
            list.addAll(cs);
        }
        return list;
    }

    private List<AgentController> loadControllers(List<PersistentSolution> solutions, int subpop, int sampleFreq) throws Exception {
        if (sampleFreq == 1) {
            ArrayList<AgentController> list = new ArrayList<AgentController>(solutions.size());
            for (PersistentSolution s : solutions) {
                list.add(getAC(s, subpop));
            }
            return list;
        }

        ArrayList<AgentController> list = new ArrayList<AgentController>();
        if (sampleFreq > 0) {
            list.add(getAC(solutions.get(solutions.size() - 1), subpop));
            Random rand = new Random();
            int splits = solutions.size() / sampleFreq;
            for (int i = 0; i < splits; i++) {
                int index = i * sampleFreq + rand.nextInt(sampleFreq);
                list.add(getAC(solutions.get(index), subpop));
            }
        }
        return list;
    }

    private AgentController getAC(PersistentSolution s, int subpop) {
        HeterogeneousGroupController gc = (HeterogeneousGroupController) s.getController();
        return gc.getAgentControllers(2)[subpop];
    }

    public List<EvaluationResult[]> tournament(List<AgentController> host, List<AgentController> parasite, int hostSub) throws InterruptedException, ExecutionException {
        List<Worker> workers = new ArrayList<Worker>();
        for (int i = 0; i < host.size(); i++) {
            workers.add(new Worker(host.get(i), parasite, hostSub));
        }
        List<Future<EvaluationResult[]>> results = executor.invokeAll(workers);

        ArrayList<EvaluationResult[]> evals = new ArrayList<EvaluationResult[]>(host.size());
        for (Future<EvaluationResult[]> f : results) {
            evals.add(f.get());
        }
        System.out.println();
        return evals;
    }

    private class Worker implements Callable<EvaluationResult[]> {

        private final AgentController host;
        private final List<AgentController> parasites;
        private final int hostSub;

        public Worker(AgentController host, List<AgentController> parasites, int hostSub) {
            this.host = host;
            this.parasites = parasites;
            this.hostSub = hostSub;
        }

        @Override
        public EvaluationResult[] call() throws Exception {
            System.out.print(".");
            EvaluationResult[][] iRes = null;
            for (int j = 0; j < parasites.size(); j++) {
                HeterogeneousGroupController gc = hostSub == 0
                        ? new HeterogeneousGroupController(new AgentController[]{host, parasites.get(j)})
                        : new HeterogeneousGroupController(new AgentController[]{parasites.get(j), host});
                EvaluationResult[] eval = sim.evaluateSolution(gc, 0);
                if (iRes == null) {
                    iRes = new EvaluationResult[eval.length][parasites.size()];
                }
                for (int k = 0; k < eval.length; k++) {
                    iRes[k][j] = eval[k];
                }
            }

            EvaluationResult[] merged = new EvaluationResult[iRes.length];
            for (int k = 0; k < iRes.length; k++) {
                merged[k] = iRes[k][0].mergeEvaluations(iRes[k]);
            }
            return merged;
        }
    }
}
