/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class GoScoreBehaviour extends MasonEvaluation {

    private SubpopEvaluationResult ser;

    @Override
    protected void postSimulation(MasonSimState sim) {
        Go go = (Go) sim;
        double d = go.schedule.getSteps() / 2.0f;
        double s = FastMath.pow2(go.boardSize);
        ser = new SubpopEvaluationResult(
                new VectorBehaviourResult(
                        go.state.captured[GoState.BLACK] / d,
                        go.state.possession[GoState.BLACK] / s,
                        go.state.possession[GoState.BLACK] == 0 ? 0 : go.state.surrounded[GoState.BLACK] / (double) go.state.possession[GoState.BLACK]),
                new VectorBehaviourResult(
                        go.state.captured[GoState.WHITE] / d,
                        go.state.possession[GoState.WHITE] / s,
                        go.state.possession[GoState.WHITE] == 0 ? 0 : go.state.surrounded[GoState.WHITE] / (double) go.state.possession[GoState.WHITE]));
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }
}
