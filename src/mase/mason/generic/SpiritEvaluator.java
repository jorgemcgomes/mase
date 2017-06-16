/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.CompoundEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.SmartAgent;
import net.jafama.FastMath;
import org.apache.commons.math3.util.ArithmeticUtils;

/**
 * Almost never visited sensory states can be ignored -- they are irrelevant for
 * the distance calculation
 * Remove sensory states that were visited less than X% of the time 
 * @author jorge
 */
public class SpiritEvaluator extends MasonEvaluation<CompoundEvaluationResult<SpiritResult>> {

    public static final String P_SENSOR_BINS = "sensor-bins";
    public static final String P_ACTION_BINS = "action-bins";
    public static final String P_FILTER = "filter";

    private static final long serialVersionUID = 1L;
    private int sensorBins, actionBins;
    private List<? extends SmartAgent> agents;
    private int[] sensorPows;
    private int[] actionPows;
    
    // Filter only on merging
    // merging is simply adding the count matrixes
    private double filter;
    
    private CompoundEvaluationResult<SpiritResult> res;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.sensorBins = state.parameters.getInt(base.push(P_SENSOR_BINS), null);
        this.actionBins = state.parameters.getInt(base.push(P_ACTION_BINS), null);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), null);
        this.sensorPows = new int[10];
        this.actionPows = new int[10];
        for(int i = 0 ; i < 10 ; i++) {
            sensorPows[i] = ArithmeticUtils.pow(sensorBins, i);
            actionPows[i] = ArithmeticUtils.pow(actionBins, i);
        }
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim); 
        SmartAgentProvider sp = (SmartAgentProvider) sim;
        agents = sp.getSmartAgents();
        ArrayList<SpiritResult> list = new ArrayList<>();
        for (SmartAgent ag : agents) {
            SpiritResult sr = new SpiritResult(ArithmeticUtils.pow(sensorBins, ag.getNInputs()), 
                    ArithmeticUtils.pow(actionBins, ag.getNOutputs()));
            sr.setFilterThreshold(filter);
            //sr.allowLoging(true); 
            list.add(sr);
        }
        res = new CompoundEvaluationResult<>(list);        
    }
    

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        for(int i = 0 ; i < agents.size() ; i++) {
            SmartAgent ag = agents.get(i);
            SpiritResult sr = res.getEvaluation(i);
            int sI = sensorIndex(ag.lastNormalisedInputs());
            int aI = actionIndex(ag.lastNormalisedOutputs());
            sr.addEntry(sI, aI);
        }      
    }
        
    @Override
    public CompoundEvaluationResult<SpiritResult> getResult() {
        return res;
    }

    // from bin-ary numeral system to decimal
    protected int sensorIndex(double[] sensors) {
        byte[] r = new byte[sensors.length];
        for (int i = 0; i < sensors.length; i++) {
            // scale from [-1,1] to [0,2] to [0,1] to [0,bins-1]   
            r[i] = (byte) FastMath.round((FastMath.min(FastMath.max(sensors[i], -1), 1) + 1) / 2 * (sensorBins - 1));
        }
        
        int index = 0;
        int n = r.length;
        for(int i = 0 ; i < n ; i++) {
            index += r[i] * sensorPows[n - i - 1];
        }
        return index;
    }

    protected int actionIndex(double[] actions) {
                byte[] r = new byte[actions.length];
        for (int i = 0; i < actions.length; i++) {
            // scale from [0,1] to [0,bins-1]
            r[i] = (byte) FastMath.round(FastMath.min(FastMath.max(actions[i], 0), 1) * (actionBins - 1));
        }   
        
        int index = 0;
        int n = r.length;
        for(int i = 0 ; i < n ; i++) {
            index += r[i] * actionPows[n - i - 1];
        }
        return index;
    }
}
