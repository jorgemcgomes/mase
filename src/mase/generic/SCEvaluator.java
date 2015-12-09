/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.HashMap;
import java.util.List;
import mase.evaluation.VectorBehaviourResult;
import static mase.evaluation.VectorBehaviourResult.V_BRAY_CURTIS;
import static mase.evaluation.VectorBehaviourResult.V_COSINE;
import static mase.evaluation.VectorBehaviourResult.V_EUCLIDEAN;
import mase.mason.MasonEvaluation;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class SCEvaluator extends MasonEvaluation {

    public static final String P_DISCRETIZATION = "discretisation";
    public static final String P_DISTANCE = "distance";
    private static final long serialVersionUID = 1L;
    private int bins;
    private int distance;
    private HashMap<Integer, byte[]> key;
    private HashMap<Integer, Double> count;
    private SCResult res;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = defaultBase();
        this.bins = state.parameters.getInt(base.push(P_DISCRETIZATION), df.push(P_DISCRETIZATION));
        String dist = state.parameters.getString(base.push(P_DISTANCE), df.push(P_DISTANCE));
        switch(dist) {
            case V_EUCLIDEAN:
                distance = VectorBehaviourResult.EUCLIDEAN;
                break;
            case V_BRAY_CURTIS:
                distance = VectorBehaviourResult.BRAY_CURTIS;
                break;
            case V_COSINE:
                distance = VectorBehaviourResult.COSINE;
                break;
            default:
                state.output.fatal("Unknown distance measure.", base.push(P_DISTANCE));
        }
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(SCPostEvaluator.P_STATECOUNT_BASE);
    }

    @Override
    public SCResult getResult() {
        return res;
    }

    @Override
    protected void preSimulation() {
        this.key = new HashMap<>(100);
        this.count = new HashMap<>(100);
    }

    @Override
    protected void evaluate() {
        SmartAgentProvider td = (SmartAgentProvider) sim;
        List<? extends SmartAgent> agents = td.getSmartAgents();
        for (SmartAgent a : agents) {
            double[] lastSensors = a.lastInputs();
            double[] lastAction = a.lastOutputs();
            byte[] d = discretise(lastSensors, lastAction);
            int h = hashVector(d);
            Double c = count.get(h);
            if (c == null) {
                key.put(h, d);
                c = 0d;
            }
            count.put(h, c + 1);
        }
    }

    @Override
    protected void postSimulation() {
        res = new SCResult(count, key, distance);
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
