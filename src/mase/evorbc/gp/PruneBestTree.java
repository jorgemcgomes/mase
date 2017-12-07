/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.evaluation.EvaluationResult;
import mase.mason.MasonSimulationProblem;
import mase.stat.PersistentSolution;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class PruneBestTree extends Statistics {

    public static final String P_REPETITIONS = "repetitions";
    public static final String P_PRUNED_FILE = "prunedbest.xml";

    private static final long serialVersionUID = 1L;

    private int repetitions;
    private String prunedFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.repetitions = state.parameters.getInt(base.push(P_REPETITIONS), null);
        this.prunedFile = state.parameters.getString(base.push(P_PRUNED_FILE), null);
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        MasonSimulationProblem prob = (MasonSimulationProblem) state.evaluator.p_problem.clone();
        try {
            int log = state.output.addLog(new File("postbest.xml"), false);
            File bestFile = state.output.getLog(log).filename;
            if(!bestFile.exists()) {
                log = state.output.addLog(new File("best.xml"), false);
                bestFile = state.output.getLog(log).filename;    
                if(!bestFile.exists()) {
                    state.output.warning("Best controller not found ("+bestFile.getAbsolutePath()+"). Not prunning.");
                    return;
                }
            }
            
            PersistentSolution bestSol = SolutionPersistence.readSolutionFromFile(bestFile);

            // Prune the controller
            GPArbitratorController c = (GPArbitratorController) bestSol.getController().getAgentControllers(1)[0];
            pruneController(c, prob, repetitions);

            // Write the new pruned controller
            log = state.output.addLog(new File(prunedFile), false);
            File out = state.output.getLog(log).filename;

            PersistentSolution newP = bestSol.clone();
            newP.setController(new HomogeneousGroupController(c));
            SolutionPersistence.writeSolution(newP, out);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int reps = ReevaluationTools.getRepetitions(args);
        File gc = ReevaluationTools.findControllerFile(args);
        PersistentSolution sol = SolutionPersistence.readSolutionFromFile(gc);
        GroupController controller = sol.getController();
        MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, gc.getParentFile());

        GPArbitratorController c = (GPArbitratorController) controller.getAgentControllers(1)[0];
        System.out.println("----------- ORIGINAL ------------");
        System.out.println(c);

        pruneController(c, simulator, reps);

        System.out.println("----------- PRUNED ------------");
        System.out.println(c);
        
        File out = new File(gc.getAbsolutePath().replace("postbest", "prunedbest"));
        PersistentSolution newP = sol.clone();
        newP.setController(new HomogeneousGroupController(c));
        SolutionPersistence.writeSolution(newP, out);
    }

    public static void pruneController(GPArbitratorController c, MasonSimulationProblem simulator, int reps) {
        try {
            // add the node logger
            UsedNodesEvaluation e = new UsedNodesEvaluation();
            int evalIndex = simulator.getEvalFunctions().length;
            simulator.setEvalFunctions(ArrayUtils.add(simulator.getEvalFunctions(), e));
            simulator.setRepetitions(reps);
            EvaluationResult[] res = simulator.evaluateSolution(new HomogeneousGroupController(c), 0);
            NodeSetResult ue = (NodeSetResult) res[evalIndex];
            CleanupCodePostEvaluator.prune(c.getProgramTree().child, ue.value());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
