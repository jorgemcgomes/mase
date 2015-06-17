/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.SimulationProblem;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimulator extends SimulationProblem {

    public static final String P_MAX_STEPS = "max-steps";
    protected int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        base = defaultBase();
        maxSteps = state.parameters.getInt(base.push(P_MAX_STEPS), null);
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
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
