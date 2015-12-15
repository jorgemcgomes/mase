/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import sim.engine.SimState;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimState extends SimState {

    private static final long serialVersionUID = 1L;
    
    protected GroupController gc;

    public MasonSimState(GroupController gc, long seed) {
        super(seed);
        this.gc = gc;
    }

    public EvaluationResult[] evaluate(int repetitions, int maxSteps, EvaluationFunction[] evalFunctions) {
        EvaluationResult[][] evalResults = new EvaluationResult[evalFunctions.length][repetitions];
        for (int r = 0; r < repetitions; r++) {
            MasonEvaluation[] evals = new MasonEvaluation[evalFunctions.length];
            for (int i = 0; i < evalFunctions.length; i++) {
                evals[i] = (MasonEvaluation) evalFunctions[i].clone();
                evals[i].setSimulationModel(this);
            }

            start();
            for (int i = 0; i < evals.length; i++) {
                evals[i].preSimulation();
            }
            while (schedule.getSteps() < maxSteps) {
                boolean b = schedule.step(this);
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
            finish();
        }

        EvaluationResult[] mergedResult = new EvaluationResult[evalFunctions.length];
        for (int i = 0; i < evalFunctions.length; i++) {
            mergedResult[i] = evalResults[i][0].mergeEvaluations(evalResults[i]);
        }
        return mergedResult;
    }

    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    public abstract void setupPortrayal(FieldPortrayal2D port);

}
