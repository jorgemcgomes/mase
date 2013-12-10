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
import mase.controllers.AgentController;
import mase.controllers.HeterogeneousGroupController;
import mase.evaluation.EvaluationResult;
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
            if(args[x].equalsIgnoreCase(FREQUENCY)) {
                freq = Integer.parseInt(args[1 + x++]);
            }
        }
        if(folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }
        
        MasonSimulator sim = MasonPlayer.createSimulator(args);
        makeTournaments(folders, freq, sim);
    }
    
    public static void makeTournaments(List<File> folders, int sampleFreq, MasonSimulator sim) throws Exception {
        List<File> tars0 = new ArrayList<File>();
        List<File> tars1 = new ArrayList<File>();
        
        for(File folder : folders) {
            int i = 0;
            while(true) {
                File b0 = new File(folder, "job."+i+".bests.0.tar.gz");
                if(b0.exists()) {
                    File b1 = new File(folder, "job."+i+".bests.1.tar.gz");
                    tars0.add(b0);
                    tars1.add(b1);
                    i++;
                } else {
                    break;
                }
            }
        }
        
        List<AgentController> sample0 = loadSample(tars0, 0, sampleFreq);
        List<AgentController> sample1 = loadSample(tars1, 1, sampleFreq);
        
        for(int i = 0 ; i < tars0.size() ; i++) {
            File tar = tars0.get(i);
            List<AgentController> all = loadControllers(tar, 0, 1);
            System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + sample1.size());
            List<EvaluationResult[]> evals0 = tournament(all, sample1, 0, sim);
            tar = tars1.get(i);
            all = loadControllers(tar, 1, 1);
            System.out.println(tar.getAbsolutePath() + " " + all.size() + " vs " + sample0.size());
            List<EvaluationResult[]> evals1 = tournament(all, sample0, 1, sim);
            
            File log = new File(tar.getParent(), tar.getName().replace("bests.1.tar.gz", "comp.stat"));
            BufferedWriter bfw = new BufferedWriter(new FileWriter(log));
            for(int g = 0 ; g < evals0.size() ; g++) {
                EvaluationResult[] e0 = evals0.get(g);
                EvaluationResult[] e1 = evals1.get(g);
                float fit0 = (Float) (((SubpopEvaluationResult) e0[0]).getSubpopEvaluation(0).value());
                float fit1 = (Float) (((SubpopEvaluationResult) e1[0]).getSubpopEvaluation(1).value());
                bfw.write(g + " " + fit0 + " " + fit1);
                bfw.newLine();
            }
            bfw.close();
        }
    }
    
    
    private static List<AgentController> loadSample(List<File> tars, int subpop, int sampleFreq) throws Exception {
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        for(File f : tars) {
            List<AgentController> cs = loadControllers(f, subpop, sampleFreq);
            list.addAll(cs);
        }
        return list;
    }
    

    private static List<AgentController> loadControllers(File tar, int subpop, int sampleFreq) throws Exception {
        ArrayList<AgentController> list = new ArrayList<AgentController>();
        List<PersistentSolution> solutions = SolutionPersistence.readSolutionsFromTar(tar);
        
        for(int i = 0 ; i < solutions.size() ; i++) {
            if(i % sampleFreq == 0 || i == solutions.size() - 1) {
                PersistentSolution s = solutions.get(i);
                HeterogeneousGroupController gc = (HeterogeneousGroupController) s.getController();
                list.add(gc.getAgentControllers(2)[subpop]);
            }
        }
        return list;
    }

    public static List<EvaluationResult[]> tournament(List<AgentController> host, List<AgentController> parasite, int hostSub, MasonSimulator sim)  {
        ArrayList<EvaluationResult[]> evals = new ArrayList<EvaluationResult[]>(host.size());
        for (int i = 0; i < host.size(); i++) {
            System.out.print(".");
            EvaluationResult[][] iRes = null;
            for (int j = 0; j < parasite.size(); j++) {
                HeterogeneousGroupController gc = hostSub == 0 ? 
                        new HeterogeneousGroupController(new AgentController[]{host.get(i), parasite.get(j)}) :
                        new HeterogeneousGroupController(new AgentController[]{parasite.get(j), host.get(i)});
                EvaluationResult[] eval = sim.evaluateSolution(gc, 0);
                if (iRes == null) {
                    iRes = new EvaluationResult[eval.length][parasite.size()];
                }
                for (int k = 0; k < eval.length; k++) {
                    iRes[k][j] = eval[k];
                }
            }

            EvaluationResult[] merged = new EvaluationResult[iRes.length];
            for (int k = 0; k < iRes.length; k++) {
                merged[k] = iRes[k][0].mergeEvaluations(iRes[k]);
            }
            evals.add(merged);
        }
        System.out.println();
        return evals;
    }
}
