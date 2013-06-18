/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.GroupController;
import mase.Simulator;
import sim.display.GUIState;
import sim.engine.SimState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimulator implements Simulator {

    public static final String P_EVAL_NUMBER = "number-evals";
    public static final String P_EVAL = "eval";
    protected MasonEvaluation[] evalProtos;
    public static final String P_MAX_STEPS = "max-steps";
    public static final String P_REPETITIONS = "repetitions";
    protected int maxSteps;
    protected int repetitions;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        /* Generic simulation parameters */
        
        if (!state.parameters.exists(base.push(P_MAX_STEPS))) {
            state.output.fatal("Parameter not found.", base.push(P_MAX_STEPS));
        }
        maxSteps = state.parameters.getInt(base.push(P_MAX_STEPS), null);
        repetitions = state.parameters.getIntWithDefault(base.push(P_REPETITIONS), null, 1);
        if (repetitions < 1) {
            state.output.fatal("Parameter invalid value. Must be > 0.", base.push(P_REPETITIONS));
        }

        /* Evaluation */

        if(!state.parameters.exists(base.push(P_EVAL_NUMBER), null)) {
            state.output.warning("Parameter not exists. Going to use just 1 evaluation.", base.push(P_EVAL_NUMBER));
        }
        int nEvals = state.parameters.getIntWithDefault(base.push(P_EVAL_NUMBER), null, 1);
        evalProtos = new MasonEvaluation[nEvals];
        for (int i = 0; i < nEvals; i++) {
            evalProtos[i] = (MasonEvaluation) state.parameters.getInstanceForParameter(base.push(P_EVAL).push("" + i), base.push(P_EVAL), MasonEvaluation.class);
            evalProtos[i].setup(state, base.push(P_EVAL).push("" + i));
        }
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        SimState sim = createSimState(gc, seed);
        EvaluationResult[][] evalResults = new EvaluationResult[evalProtos.length][repetitions];
        for (int r = 0; r < repetitions; r++) {
            MasonEvaluation[] evals = new MasonEvaluation[evalProtos.length];
            for (int i = 0; i < evalProtos.length; i++) {
                evals[i] = (MasonEvaluation) evalProtos[i].clone();
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

        EvaluationResult[] mergedResult = new EvaluationResult[evalProtos.length];
        for (int i = 0; i < evalProtos.length; i++) {
            mergedResult[i] = evalResults[i][0].mergeEvaluations(evalResults[i]);
        }
        return mergedResult;
    }

    public abstract SimState createSimState(GroupController gc, long seed);

    public abstract GUIState createSimStateWithUI(GroupController gc, long seed);
}
