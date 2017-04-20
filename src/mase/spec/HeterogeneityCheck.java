/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.neat.NEATAgentController;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.neat4j.neat.nn.core.Synapse;

/**
 *
 * @author jorge
 */
public class HeterogeneityCheck {

    public static final String FOLDER = "-f";
    public static final String AGENTS = "-a";

    public static void main(String[] args) throws Exception {
        List<File> folders = new ArrayList<File>();
        int agents = 0;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(AGENTS)) {
                agents = Integer.parseInt(args[1 + x++]);
            }
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        for (File f : folders) {
            reevaluateFolder(f, agents);
        }
    }

    public static void reevaluateFolder(File folder, int agents) throws Exception {
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
            File log = new File(tar.getParent(), tar.getName().replace("bests.tar.gz", "hetero.stat"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(log));

            // Check solutions
            for (int i = 0; i < sols.size(); i++) {
                PersistentSolution sol = sols.get(i);

                // check heterogeneity
                GroupController controller = sol.getController();
                AgentController[] acs = controller.getAgentControllers(agents);
                HashSet<Double> set = new HashSet<Double>();
                for (AgentController ac : acs) {
                    NEATAgentController nac = (NEATAgentController) ac;
                    Synapse[] connections = nac.getNetwork().connections();
                    double syn = 0;
                    for (Synapse s : connections) {
                        syn += s.getWeight();
                    }
                    set.add(syn);
                }

                writer.write(i + " " + sol.getFitness() + " " + set.size() + " " + 
                        countParticipants(sol,0.1333) + " " + countParticipants(sol,0.2)); 
                writer.newLine();
            }
            writer.close();
        }
    }

    private static int countParticipants(PersistentSolution sol, double threshold) {
        // check agent characterisation
        EvaluationResult[] evals = sol.getEvalResults();
        CompoundEvaluationResult e = (CompoundEvaluationResult) evals[2];
        int participant = 0;
        for (EvaluationResult er : e.value()) { // for each agent
            VectorBehaviourResult vbr = (VectorBehaviourResult) er;
            double[] behaviour = vbr.getBehaviour();
            for (int b = 1; b < behaviour.length; b++) { // for each distance
                if (behaviour[b] <= threshold) {
                    participant++;
                    break;
                }
            }
        }
        return participant;
    }
}
