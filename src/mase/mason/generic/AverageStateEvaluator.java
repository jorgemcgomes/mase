/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.SmartAgent;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class AverageStateEvaluator extends MasonEvaluation<CompoundEvaluationResult<VectorBehaviourResult>> {

    private static final long serialVersionUID = 1L;
    public static final String P_WINDOWS = "windows";
    private int windows;
    private List<List<double[]>> states;
    private CompoundEvaluationResult<VectorBehaviourResult> res;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        windows = state.parameters.getInt(base.push(P_WINDOWS), null);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        states = new ArrayList<>(super.maxEvaluationSteps);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        SmartAgentProvider td = (SmartAgentProvider) sim;
        List<? extends SmartAgent> agents = td.getSmartAgents();
        List<double[]> genStates = new ArrayList<>();
        for (SmartAgent a : agents) {
            double[] lastSensors = a.lastNormalisedInputs();
            double[] lastAction = a.lastNormalisedOutputs();
            double[] vec = new double[lastSensors.length + lastAction.length];
            for (int i = 0; i < lastSensors.length; i++) {
                vec[i] = (lastSensors[i] + 1) / 2; // scale sensor values from [-1,1] to [0,1]
            }
            System.arraycopy(lastAction, 0, vec, lastSensors.length, lastAction.length);
            genStates.add(vec);
        }
        states.add(genStates);
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        List<double[]> concats = new ArrayList<>(states.get(0).size());
        for(int ag = 0 ; ag < states.get(0).size() ; ag++) {
            concats.add(new double[]{});
        }
        
        int windowStep = states.size() / windows;
        for(int w =  0; w < windows ; w++) {
            int from = w * windowStep;
            int to = (w == windows - 1) ? states.size() : from + windowStep;
            List<double[]> avgs = averageStates(from, to);
            
            for(int ag = 0 ; ag < concats.size() ; ag++) {
                double[] v1 = concats.get(ag);
                double[] v2 = avgs.get(ag);
                concats.set(ag, ArrayUtils.addAll(v1, v2));
            }
        }
        
        Collection<VectorBehaviourResult> resList = new ArrayList<>();
        for(double[] c : concats) {
            VectorBehaviourResult vbr = new VectorBehaviourResult(c);
            vbr.setDistance(VectorBehaviourResult.Distance.euclidean);
            vbr.setLocationEstimator(VectorBehaviourResult.LocationEstimator.mean);
            resList.add(vbr);
        }
        this.res = new CompoundEvaluationResult<>(resList);
    }

    // excluding to
    private List<double[]> averageStates(int from, int to) {
        // Init vectors with zeros
        List<double[]> avg = new ArrayList<>();
        for(int ag = 0 ; ag < states.get(0).size() ; ag++) {
            avg.add(new double[states.get(0).get(ag).length]);
        }
        
        // sum the vectors agent-wise
        for(int i = from ; i < to ; i++) {
            List<double[]> step = states.get(i);
            for(int ag = 0 ; ag < step.size() ; ag++) {
                for(int j = 0 ; j < avg.get(ag).length ; j++) {
                    avg.get(ag)[j] += step.get(ag)[j]; 
                }
            }            
        }
        
        // divide by the number of steps
        for(double[] v : avg) {
            for(int i = 0 ; i < v.length ; i++) {
                v[i] /= (to - from);
            }
        }
        return avg;
    }

    @Override
    public CompoundEvaluationResult<VectorBehaviourResult> getResult() {
        return res;
    }

}
