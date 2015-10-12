/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.app.go.GoState.Group;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;

/**
 *
 * @author jorge
 */
public class GoGroupCharacterisation extends MasonEvaluation {

    private SubpopEvaluationResult ser;
    protected double maxSteps;
    protected double[] biggestGroupSize;
    protected double[] groupNumber;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
    }

    @Override
    protected void preSimulation() {
        this.biggestGroupSize = new double[2];
        this.groupNumber = new double[2];
    }

    @Override
    protected void evaluate() {
        Go go = (Go) sim;
        for (int i = 0; i < 2; i++) {
            int sizeLargest = -1;
            int allStones = 0;
            for (Group g : go.state.groups[i]) {
                int size = g.stones.size();
                if (size > sizeLargest) {
                    sizeLargest = size;
                }
                allStones += size;
            }
            if (sizeLargest != -1) {
                biggestGroupSize[i] += sizeLargest / allStones;
            }
            groupNumber[i] += allStones > 0 ? go.state.groups[i].size() / allStones : 0;
        }
    }

    @Override
    protected void postSimulation() {
        Go go = (Go) sim;
        double d = go.schedule.getSteps() / 2.0f;
        ser = new SubpopEvaluationResult(
                new VectorBehaviourResult(
                        go.state.captured[GoState.BLACK] / d,
                        go.state.captured[GoState.WHITE] / d,
                        go.schedule.getSteps() / maxSteps,
                        biggestGroupSize[GoState.BLACK] / currentEvaluationStep,
                        groupNumber[GoState.BLACK] / currentEvaluationStep),
                new VectorBehaviourResult(
                        go.state.captured[GoState.WHITE] / d,
                        go.state.captured[GoState.BLACK] / d,
                        go.schedule.getSteps() / maxSteps,
                        biggestGroupSize[GoState.WHITE] / currentEvaluationStep,
                        groupNumber[GoState.WHITE] / currentEvaluationStep));
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
