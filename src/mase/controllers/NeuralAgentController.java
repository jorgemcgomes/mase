/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import java.util.Arrays;
import org.encog.neural.flat.FlatNetwork;

/**
 *
 * @author jorge
 */
public class NeuralAgentController implements AgentController {

    private static final long serialVersionUID = 1;
    private final FlatNetwork network;

    public NeuralAgentController(FlatNetwork net) {
        this.network = net;
    }

    @Override
    public void reset() {
        network.clearContext();
    }

    @Override
    public double[] processInputs(double[] input) {
        double[] output = new double[network.getOutputCount()];
        network.compute(input, output);
        return output;
    }

    @Override
    public String toString() {
        return Arrays.toString(network.encodeNetwork());
    }

    @Override
    public AgentController clone() {
        return new NeuralAgentController(network.clone());
    }
}
