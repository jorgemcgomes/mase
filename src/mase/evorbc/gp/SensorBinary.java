/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class SensorBinary extends GPNode {

    public static final String P_INDEX = "sensor-index";
    public static final String P_NAME = "sensor-name";
    private static final long serialVersionUID = 1L;
    private int index;
    private String name = null;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        index = state.parameters.getInt(base.push(P_INDEX), defaultBase().push(P_INDEX));
        if (state.parameters.exists(base.push(P_NAME), defaultBase().push(P_NAME))) {
            name = state.parameters.getString(base.push(P_NAME), defaultBase().push(P_NAME));
        }
    }

    @Override
    public String toString() {
        return name == null ? "S" + index : name;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        if (d.sensorValues[index] > 0) {
            children[0].eval(state, thread, input, stack, individual, problem);
        } else {
            children[1].eval(state, thread, input, stack, individual, problem);
        }
    }

    @Override
    public int expectedChildren() {
        return 2;
    }

}
