/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import mase.controllers.AgentController;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.nn.core.NeuralNetFactory;
import org.neat4j.neat.nn.core.Synapse;

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

    @Override
    public String toString() {
        int selfRecurr = 0;
        int recurr = 0;
        for (Synapse s : network.connections()) {
            if (((NEATNeuron) s.getFrom()).id() == ((NEATNeuron) s.getTo()).id()) {
                selfRecurr++;
            }
            if(((NEATNeuron) s.getFrom()).neuronDepth() < ((NEATNeuron) s.getTo()).neuronDepth()) {
                recurr++;
            }
        }
        return "Neurons:" + network.neurons().length + " Con:" + network.connections().length + " Self-rec:" + selfRecurr + " Rec:" + recurr; 
    }

}
