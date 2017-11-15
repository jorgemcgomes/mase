/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jorge
 */
public class SensorValueERC extends ERC {

    private static final long serialVersionUID = 1L;
    // list of sensor indexes, separated by commas, or total number of sensors
    public static final String P_SENSORS = "sensors";
    private int[] possibleIndexes;
    private int sensorIndex;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String sens = state.parameters.getString(base.push(P_SENSORS), defaultBase().push(P_SENSORS));
        if(StringUtils.isNumeric(sens)) { // all indexes up to the given number
            int n = Integer.parseInt(sens);
            possibleIndexes = new int[n];
            for(int i = 0 ; i < n ; i++) {
                possibleIndexes[i] = i;
            }
        } else { // only the given indexes
            String[] split = sens.split("[;,\\s+]");
            possibleIndexes = new int[split.length];
            for(int i = 0 ; i < possibleIndexes.length ; i++) {
                possibleIndexes[i] = Integer.parseInt(split[i].trim());
            }
        }
    }

    @Override
    public String name() {
        return "SV";
    }    
    
    @Override
    public String toString() {
        return "S" + sensorIndex;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        d.doubleValue = d.sensorValues[sensorIndex];
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        int rand = state.random[thread].nextInt(possibleIndexes.length);
        this.sensorIndex = possibleIndexes[rand];
    }

    @Override
    public boolean nodeEquals(GPNode node) {
        return node instanceof SensorValueERC && sensorIndex == ((SensorValueERC) node).sensorIndex;
    }

    @Override
    public String encode() {
        return toString();
    }
}
