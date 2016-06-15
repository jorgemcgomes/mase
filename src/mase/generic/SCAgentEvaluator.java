/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class SCAgentEvaluator extends MasonEvaluation {

    public static final String P_DISCRETIZATION = "discretisation";
    public static final String P_FILTER = "filter";
    private static final long serialVersionUID = 1L;
    private int bins;
    private double filter;
    
    private List<HashMap<Integer, Integer>> counts;
    private SubpopEvaluationResult res;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.bins = state.parameters.getInt(base.push(P_DISCRETIZATION), null);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), null);
    }

    @Override
    public SubpopEvaluationResult getResult() {
        return res;
    }

    @Override
    protected void preSimulation() {
        this.counts = null;
    }

    @Override
    protected void evaluate() {
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
            int h = hashVector(d);
            HashMap<Integer,Integer> map = counts.get(i);
            Integer c = map.get(h);
            if (c == null) {
                c = 0;
            }
            map.put(h, c + 1);            
        }
    }

    @Override
    protected void postSimulation() {
        Collection<EvaluationResult> scs = new ArrayList<>(counts.size());
        for(HashMap<Integer, Integer> m : counts) {
            SCResult scr = new SCResult(m);
            if(filter > 0) {
                scr.filter(filter);
            }
            scs.add(scr);
        }
        res = new SubpopEvaluationResult(scs);
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

    // better: https://github.com/jpountz/lz4-java/blob/master/src/java/net/jpountz/xxhash/XXHash32JavaSafe.java
    // http://stackoverflow.com/questions/415953/generate-md5-hash-in-java
    protected int hashVector(byte[] array) {
        int hash = 0;
        for (byte b : array) {
            hash += (b & 0xFF);
            hash += (hash << 10);
            hash ^= (hash >>> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >>> 11);
        hash += (hash << 15);
        return Math.abs(hash);
    }
}
