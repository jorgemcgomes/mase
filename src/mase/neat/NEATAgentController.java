/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import mase.AgentController;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.nn.core.NeuralNetFactory;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements AgentController {

    private NEATNeuralNet network;

    public NEATAgentController(NEATNeuralNet network) {
        this.network = network;
    }
    
    public NEATNeuralNet getNetwork() {
        return network;
    }

    @Override
    public double[] processInputs(double[] input) {
        NetworkInput in = new CSVInput(input);
        NetworkOutputSet output = network.execute(in);
        return output.nextOutput().values();
    }

    @Override
    public void reset() {
        this.network = (NEATNeuralNet) NeuralNetFactory.getFactory().createNN(network.netDescriptor());
        this.network.updateNetStructure();
    }

    @Override
    public AgentController clone() {
        NEATNeuralNet newNet = (NEATNeuralNet) NeuralNetFactory.getFactory().createNN(network.netDescriptor());
        newNet.updateNetStructure();
        return new NEATAgentController(newNet);
    }
}
