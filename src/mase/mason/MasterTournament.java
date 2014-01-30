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

/**
 *
 * @author jorge
 */
public class MasterTournament {

    public static final String FOLDER = "-f";
    public static final String FREQUENCY = "-freq";
    public static final String OUTNAME = "-name";
    public static final String SELF = "-self";

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<File>();
        int freq = 1;
        String name = "comp.stat";
        boolean self = false;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(FREQUENCY)) {
                freq = Integer.parseInt(args[1 + x++]);
            } else if (args[x].equalsIgnoreCase(OUTNAME)) {
                name = args[1 + x++];
            } else if (args[x].equalsIgnoreCase(SELF)) {
                self = true;
            }
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        MasonSimulator sim = MasonPlayer.createSimulator(args);
        MasterTournament mt = new MasterTournament(folders, freq, self, sim, name);
        mt.makeTournaments();
    }
    private final List<File> folders;
    private final int freq;
    private final boolean self;
    private final MasonSimulator sim;
    private final ExecutorService executor;
    private final String name;

    public MasterTournament(List<File> folders, int sampleFreq, boolean self, MasonSimulator sim, String name) {
        this.folders = folders;
        this.freq = sampleFreq;
        this.self = self;
        this.sim = sim;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.name = name;
    }

    public void makeTournaments() throws Exception {
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

        // Load samples

        List<AgentController>[] samples = new List[2];
        if (!self) {
            samples[0] = loadSample(tars[1], 1, freq);
            samples[1] = loadSample(tars[0], 0, freq);
        }

        // Make tournaments
        for (int job = 0; job < tars[0].size(); job++) {
            // Make evaluations -- test every best from every generation against the samples
            List<EvaluationResult[]>[] subpopEvals = new List[2];
            List<PersistentSolution>[] solutions = new List[2];
            for (int s = 0; s < 2; s++) {
                if (self) {
                    int opposing = s == 0 ? 1 : 0;
                    samples[s] = loadSample(Collections.singletonList(tars[opposing].get(job)), opposing, freq);
                }

                File tar = tars[s].get(job);
                System.out.println(tar.getAbsolutePath());
                solutions[s] = SolutionPersistence.readSolutionsFromTar(tar);
                List<AgentController> all = loadControllers(solutions[s], s, 1);
                System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + samples[s].size());
                subpopEvals[s] = tournament(all, samples[s], s);
            }

            // Log results
            File log = new File(tars[0].get(job).getParent(), tars[0].get(job).getName().replace("bests.0.tar.gz", name));
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
            File superBest = new File(tars[0].get(job).getParent(), tars[0].get(job).getName().replace("bests.0.tar.gz", "challenge.ind"));
            SolutionPersistence.writeSolution(sol, superBest);
        }

        executor.shutdown();
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
        if(sampleFreq == 1) {
            ArrayList<AgentController> list = new ArrayList<AgentController>(solutions.size());
            for(PersistentSolution s : solutions) {
                list.add(getAC(s, subpop));
            }
            return list;
        }
        
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        list.add(getAC(solutions.get(solutions.size() - 1), subpop));
        Random rand = new Random();
        int splits = solutions.size() / sampleFreq;
        for (int i = 0; i < splits ; i++) {
            int index = i * sampleFreq + rand.nextInt(sampleFreq);
            list.add(getAC(solutions.get(index), subpop));
        }

        /*for (int i = 0; i < solutions.size(); i++) {
         if (i % sampleFreq == 0 || i == solutions.size() - 1) {
         PersistentSolution s = solutions.get(i);
         HeterogeneousGroupController gc = (HeterogeneousGroupController) s.getController();
         list.add(gc.getAgentControllers(2)[subpop]);
         }
         }*/
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
