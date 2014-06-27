/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import org.encog.neural.networks.BasicNetwork;

/**
 *
 * @author jorge
 */
public class NeuralAgentController implements AgentController {

    private static final long serialVersionUID = 1;
    private final BasicNetwork network;

    public NeuralAgentController(BasicNetwork net) {
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
        return network.dumpWeights();
    }

    @Override
    public AgentController clone() {
        return new NeuralAgentController((BasicNetwork) network.clone());
    }
}
