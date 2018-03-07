/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class MedianStateEvaluator extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    // agent / sensor-effector value / time-step
    private double[][][] states;
    private EvaluationResult res;
    private List<? extends SmartAgent> agents;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        SmartAgentProvider td = (SmartAgentProvider) sim;
        agents = td.getSmartAgents();
        states = new double[agents.size()][][];
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        for (int a = 0; a < states.length; a++) {
            double[] state = state(agents.get(a));
            if (states[a] == null) {
                states[a] = new double[state.length][maxEvaluationSteps];
            }
            for (int v = 0; v < state.length; v++) {
                states[a][v][currentEvaluationStep] = state[v];
            }
        }
    }

    private double[] state(SmartAgent a) {
        double[] lastSensors = a.lastNormalisedInputs();
        double[] lastAction = a.lastNormalisedOutputs();
        double[] vec = new double[lastSensors.length + lastAction.length];
        for (int i = 0; i < lastSensors.length; i++) {
            vec[i] = (lastSensors[i] + 1) / 2; // scale sensor values from [-1,1] to [0,1]
        }
        System.arraycopy(lastAction, 0, vec, lastSensors.length, lastAction.length);
        return vec;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        // needs to truncate arrays
        if (currentEvaluationStep < maxEvaluationSteps) {
            for (double[][] agentState : states) {
                for (int v = 0; v < agentState.length; v++) {
                    agentState[v] = Arrays.copyOf(agentState[v], currentEvaluationStep);
                }
            }
        }
        if (states.length == 1) {
            res = new MedianVectorBehaviourResult(states[0]);
        } else {
            List<MedianVectorBehaviourResult> list = new ArrayList<>(states.length);
            for (double[][] agentState : states) {
                MedianVectorBehaviourResult m = new MedianVectorBehaviourResult(agentState);
                list.add(m);
            }
            res = new CompoundEvaluationResult(list);
        }
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
