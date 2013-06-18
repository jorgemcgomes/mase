/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import mase.AgentController;
import java.util.Arrays;
import org.encog.neural.flat.FlatNetwork;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NeuralAgentController implements AgentController {

    private FlatNetwork network;

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
}
