/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import mase.controllers.AgentController;
import mase.controllers.EncodableAgentController;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.nn.core.Synapse;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements EncodableAgentController {

    private static final long serialVersionUID = 1;
    private NEATNeuralNet network;
    
    public NEATAgentController() {
        this.network = null;
    }

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
        this.network.updateNetStructure();
    }

    @Override
    public AgentController clone() {
        try {
            NEATAgentController ac = (NEATAgentController) super.clone();
            ac.network = new NEATNeuralNet();
            ac.network.createNetStructure(network.netDescriptor());
            return ac;
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    @Override
    public String toString() {
        int selfRecurr = 0;
        int recurr = 0;
        network.updateNetStructure();
        for (Synapse s : network.connections()) {
            if (((NEATNeuron) s.getFrom()).id() == ((NEATNeuron) s.getTo()).id()) {
                selfRecurr++;
            }
            if (((NEATNeuron) s.getFrom()).neuronDepth() < ((NEATNeuron) s.getTo()).neuronDepth()) {
                recurr++;
            }
        }
        return "{Neurons:" + network.neurons().length + " Links:" + network.connections().length
                + " SelfRec:" + selfRecurr + " Rec:" + recurr + "}";/* + "\n\n"
                + NEATSerializer.serializeToString(network)*/
    }

    @Override
    public double[] encode() {
        return NEATSerializer.serializeToArray(network);
    }

    @Override
    public void decode(double[] params) {
        NEATNeuralNet net = NEATSerializer.deserialize(params);
        this.network = net;
    }

}
