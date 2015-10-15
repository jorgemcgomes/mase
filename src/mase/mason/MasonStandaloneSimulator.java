/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.Serializable;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public abstract class MasonStandaloneSimulator implements Serializable {

    public EvaluationResult[] evaluateSolution(GroupController gc, long seed, int repetitions, int maxSteps, EvaluationFunction[] evalFunctions) {
        GUICompatibleSimState sim = createSimState(gc, seed);
        EvaluationResult[][] evalResults = new EvaluationResult[evalFunctions.length][repetitions];
        for (int r = 0; r < repetitions; r++) {
            MasonEvaluation[] evals = new MasonEvaluation[evalFunctions.length];
            for (int i = 0; i < evalFunctions.length; i++) {
                evals[i] = (MasonEvaluation) evalFunctions[i].clone();
                evals[i].setSimulationModel(sim);
            }

            sim.start();
            for (int i = 0; i < evals.length; i++) {
                evals[i].preSimulation();
            }
            while (sim.schedule.getSteps() < maxSteps) {
                boolean b = sim.schedule.step(sim);
                for (int i = 0; i < evals.length; i++) {
                    evals[i].evaluationStep();
                }
                if (!b) {
                    break;
                }
            }
            for (int i = 0; i < evals.length; i++) {
                evals[i].postSimulation();
                evalResults[i][r] = evals[i].getResult();
            }
            sim.finish();
        }

        EvaluationResult[] mergedResult = new EvaluationResult[evalFunctions.length];
        for (int i = 0; i < evalFunctions.length; i++) {
            mergedResult[i] = evalResults[i][0].mergeEvaluations(evalResults[i]);
        }
        return mergedResult;
    }

    public abstract GUICompatibleSimState createSimState(GroupController gc, long seed);

    public abstract GUIState createSimStateWithUI(GroupController gc, long seed);
}
