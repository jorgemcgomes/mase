/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import mase.evaluation.CompoundEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class StateCountEvaluator extends MasonEvaluation<CompoundEvaluationResult<StateCountResult>> {

    // number of bins to discretise the sensor values. 3 is a nice value (low/mid/high)
    public static final String P_DISCRETIZATION = "discretisation";
    // from 0 (no filter) to 1 (nothing passes). 0.01 (1%) is a nice value
    public static final String P_FILTER = "filter"; 
    private static final long serialVersionUID = 1L;
    private int bins;
    private double filter;
    
    private List<HashMap<Integer, Integer>> counts;
    private CompoundEvaluationResult<StateCountResult> res;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.bins = state.parameters.getInt(base.push(P_DISCRETIZATION), null);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), null);
    }

    @Override
    public CompoundEvaluationResult<StateCountResult> getResult() {
        return res;
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        this.counts = null;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        SmartAgentProvider td = (SmartAgentProvider) sim;
        List<? extends SmartAgent> agents = td.getSmartAgents();
        
        // init
        if(counts == null) {
            counts = new ArrayList<>(agents.size());
            for(int i = 0 ; i < agents.size() ; i++) {
                counts.add(new HashMap<Integer,Integer>(1000, 0.5f));
            }
        }
        
        for(int i = 0 ; i < agents.size() ; i++) {
            SmartAgent a = agents.get(i);
            double[] lastSensors = a.lastNormalisedInputs();
            double[] lastAction = a.lastNormalisedOutputs();
            byte[] d = discretise(lastSensors, lastAction);
            int h = Arrays.hashCode(d);
            HashMap<Integer,Integer> map = counts.get(i);
            Integer c = map.get(h);
            if (c == null) {
                c = 0;
            }
            map.put(h, c + 1);            
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        Collection<StateCountResult> scs = new ArrayList<>(counts.size());
        for(HashMap<Integer, Integer> m : counts) {
            StateCountResult scr = new StateCountResult(m);
            if(filter > 0) {
                scr.filter(filter);
            }
            scs.add(scr);
        }
        res = new CompoundEvaluationResult<>(scs);
    }

    protected byte[] discretise(double[] sensors, double[] actions) {
        byte[] r = new byte[sensors.length + actions.length];
        for (int i = 0; i < sensors.length; i++) {
            // scale from [-1,1] to [0,2] to [0,1] to [0,bins-1]   
            r[i] = (byte) Math.round((Math.min(Math.max(sensors[i], -1), 1) + 1) / 2 * (bins - 1));
        }
        for (int i = 0; i < actions.length; i++) {
            // scale from [0,1] to [0,bins-1]
            r[i + sensors.length] = (byte) Math.round(Math.min(Math.max(actions[i], 0), 1) * (bins - 1));
        }
        return r;
    }
}
