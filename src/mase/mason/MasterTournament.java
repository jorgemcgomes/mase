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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mase.controllers.AgentController;
import mase.controllers.HeterogeneousGroupController;
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

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<File>();
        int freq = 1;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                folders.add(new File(args[1 + x++]));
            }
            if (args[x].equalsIgnoreCase(FREQUENCY)) {
                freq = Integer.parseInt(args[1 + x++]);
            }
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        MasonSimulator sim = MasonPlayer.createSimulator(args);
        MasterTournament mt = new MasterTournament(folders, freq, sim);
        mt.makeTournaments();
    }
    
    private final List<File> folders;
    private final int freq;
    private final MasonSimulator sim;
    private final ExecutorService executor;
    
    public MasterTournament(List<File> folders, int sampleFreq, MasonSimulator sim) {
        this.folders = folders;
        this.freq = sampleFreq;
        this.sim = sim;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void makeTournaments() throws Exception {
        // Find all the relevant tars under the given folders
        List<File> tars0 = new ArrayList<File>();
        List<File> tars1 = new ArrayList<File>();
        for (File folder : folders) {
            int i = 0;
            while (true) {
                File b0 = new File(folder, "job." + i + ".bests.0.tar.gz");
                if (b0.exists()) {
                    File b1 = new File(folder, "job." + i + ".bests.1.tar.gz");
                    tars0.add(b0);
                    tars1.add(b1);
                    i++;
                } else {
                    break;
                }
            }
        }

        // Load samples
        List<AgentController> sample0 = loadSample(tars0, 0, freq);
        List<AgentController> sample1 = loadSample(tars1, 1, freq);

        // Test every best from every generation against the samples
        for (int i = 0; i < tars0.size(); i++) {
            // Evaluate subpop 0
            File tar = tars0.get(i);
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(tar);
            List<AgentController> all = loadControllers(sols, 0, 1);
            System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + sample1.size());
            List<EvaluationResult[]> evals0 = tournament(all, sample1, 0);
            int bestIndex0 = bestIndex(evals0, 0);
            PersistentSolution best0 = sols.get(bestIndex0);
            
            // Evaluate subpop 1
            tar = tars1.get(i);
            sols = SolutionPersistence.readSolutionsFromTar(tar);
            all = loadControllers(sols, 1, 1);
            System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + sample0.size());
            List<EvaluationResult[]> evals1 = tournament(all, sample0, 1);
            int bestIndex1 = bestIndex(evals1, 1);
            PersistentSolution best1 = sols.get(bestIndex1);
            
            // Write evaluations log
            File log = new File(tar.getParent(), tar.getName().replace("bests.1.tar.gz", "comp.stat"));
            BufferedWriter bfw = new BufferedWriter(new FileWriter(log));
            for (int g = 0; g < evals0.size(); g++) {
                EvaluationResult[] e0 = evals0.get(g);
                EvaluationResult[] e1 = evals1.get(g);
                float fit0 = (Float) (((SubpopEvaluationResult) e0[0]).getSubpopEvaluation(0).value());
                float fit1 = (Float) (((SubpopEvaluationResult) e1[0]).getSubpopEvaluation(1).value());
                bfw.write(g + " " + fit0 + " " + fit1);
                bfw.newLine();
            }
            bfw.close();
            
            // Persist the most interesting challenge
            HeterogeneousGroupController hc0 = (HeterogeneousGroupController) best0.getController();
            HeterogeneousGroupController hc1 = (HeterogeneousGroupController) best1.getController();
            HeterogeneousGroupController newC = new HeterogeneousGroupController(new AgentController[]{
                hc0.getAgentControllers(2)[0],
                hc1.getAgentControllers(2)[1]
            });
            SubpopEvaluationResult ser = new SubpopEvaluationResult(new EvaluationResult[] {
                new FitnessResult((Float) (((SubpopEvaluationResult) evals0.get(bestIndex0)[0]).getSubpopEvaluation(0).value())),
                new FitnessResult((Float) (((SubpopEvaluationResult) evals1.get(bestIndex1)[0]).getSubpopEvaluation(1).value())),
            });
            PersistentSolution sol = new PersistentSolution();
            sol.setController(newC);
            sol.setEvalResults(new EvaluationResult[]{ser});
            File superBest = new File(tar.getParent(), tar.getName().replace("bests.1.tar.gz", "challenge.ind"));
            SolutionPersistence.writeSolution(sol, superBest);
        }
        
        executor.shutdown();
    }
    
    private int bestIndex(List<EvaluationResult[]> evals, int subpop) {
        float best = Float.MIN_VALUE;
        int bestIndex = -1;
        for(int i = 0 ; i < evals.size() ; i++) {
            EvaluationResult[] e = evals.get(i);
            float fit = (Float) (((SubpopEvaluationResult) e[0]).getSubpopEvaluation(subpop).value());
            if(fit > best) {
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
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        for (int i = 0; i < solutions.size(); i++) {
            if (i % sampleFreq == 0 || i == solutions.size() - 1) {
                PersistentSolution s = solutions.get(i);
                HeterogeneousGroupController gc = (HeterogeneousGroupController) s.getController();
                list.add(gc.getAgentControllers(2)[subpop]);
            }
        }
        return list;
    }

    public List<EvaluationResult[]> tournament(List<AgentController> host, List<AgentController> parasite, int hostSub) throws InterruptedException, ExecutionException {
        List<Worker> workers = new ArrayList<Worker>();
        for (int i = 0; i < host.size(); i++) {
            workers.add(new Worker(host.get(i), parasite, hostSub));
        }
        List<Future<EvaluationResult[]>> results = executor.invokeAll(workers);
        
        ArrayList<EvaluationResult[]> evals = new ArrayList<EvaluationResult[]>(host.size());
        for(Future<EvaluationResult[]> f : results) {
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
