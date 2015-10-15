/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.AgentController;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.core.control.NEAT;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.nn.core.NeuralNetDescriptor;
import org.neat4j.neat.nn.core.NeuralNetFactory;
import org.neat4j.neat.nn.core.NeuralNetType;
import org.neat4j.neat.nn.core.Synapse;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements AgentController {

    private static final long serialVersionUID = 1;
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
        this.network = createNet(network.netDescriptor());
    }

    @Override
    public AgentController clone() {
        try {
            NEATAgentController ac = (NEATAgentController) super.clone();
            ac.network = createNet(network.netDescriptor());
            return ac;
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    private NEATNeuralNet createNet(NeuralNetDescriptor nnd) {
        NEATNeuralNet net = new NEATNeuralNet();
        net.createNetStructure(nnd);
        net.updateNetStructure();
        return net;
    }

    @Override
    public String toString() {
        int selfRecurr = 0;
        int recurr = 0;
        for (Synapse s : network.connections()) {
            if (((NEATNeuron) s.getFrom()).id() == ((NEATNeuron) s.getTo()).id()) {
                selfRecurr++;
            }
            if (((NEATNeuron) s.getFrom()).neuronDepth() < ((NEATNeuron) s.getTo()).neuronDepth()) {
                recurr++;
            }
        }
        return "Neurons:" + network.neurons().length + " Con:" + network.connections().length
                + " Self-rec:" + selfRecurr + " Rec:" + recurr + "\n\n"
                + NEATSerializer.serializeToString(network);
    }

}
