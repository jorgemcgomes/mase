/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import java.util.Arrays;
import java.util.Vector;
import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public class MaseNetworkWrapper extends CINeuralNetwork {

    private AgentController realController;

    public MaseNetworkWrapper(Vector<CINNInput> inputs, Vector<CINNOutput> outputs) {
        create(inputs, outputs);
    }

    public void setRealController(AgentController ac) {
        this.realController = ac;
    }

    public AgentController getRealController() {
        return realController;
    }

    @Override
    protected double[] propagateInputs(double[] inputValues) {
        //System.out.println(Arrays.toString(inputValues));
        // transform inputs from [0,1] to [-1,1]
        double[] vals = Arrays.copyOf(inputValues, inputValues.length);
        for(int i = 0 ; i < vals.length ; i++) {
            vals[i] = vals[i] * 2 - 1;
        }
        double[] out = realController.processInputs(vals);
        return out;
    }

    @Override
    public void reset() {
        realController.reset();
    }

}
