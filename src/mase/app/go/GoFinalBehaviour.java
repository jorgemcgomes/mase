/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;

/**
 *
 * @author jorge
 */
public class GoFinalBehaviour extends MasonEvaluation {

    private SubpopEvaluationResult ser;
    protected float maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
    }

    @Override
    protected void postSimulation() {
        Go go = (Go) sim;
        float d = go.schedule.getSteps() / 2.0f;
        ser = new SubpopEvaluationResult(
                new VectorBehaviourResult(
                        go.state.captured[GoState.BLACK] / d, 
                        go.state.captured[GoState.WHITE] / d),
                new VectorBehaviourResult(
                        go.state.captured[GoState.WHITE] / d, 
                        go.state.captured[GoState.BLACK] / d)
        );
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }
}
