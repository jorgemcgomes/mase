/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import java.util.Arrays;
import mase.AgentController;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.EngineArray;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements AgentController {

    private NEATNetwork network;

    public NEATAgentController(NEATNetwork network) {
        this.network = network;
    }

    @Override
    public double[] processInputs(double[] input) {
        double[] result = new double[network.getOutputCount()];

        double[] preActivation = network.getPreActivation();
        double[] postActivation = network.getPostActivation();
        NEATLink[] links = network.getLinks();

        // copy input
        EngineArray.arrayCopy(input, 0, postActivation, 1, network.getInputCount());

        // one activation cycle
        for (int j = 0; j < links.length; j++) {
            preActivation[links[j].getToNeuron()] += postActivation[links[j].getFromNeuron()] * links[j].getWeight();
        }
        for (int j = network.getOutputIndex(); j < preActivation.length; j++) {
            postActivation[j] = preActivation[j];
            network.getActivationFunctions()[j].activationFunction(postActivation, j, 1);
            preActivation[j] = 0.0F;
        }

        // copy output
        EngineArray.arrayCopy(postActivation, network.getOutputIndex(), result, 0, network.getOutputCount());
        
        return result;
    }

    @Override
    public void reset() {
        EngineArray.fill(network.getPreActivation(), 0.0);
        EngineArray.fill(network.getPostActivation(), 0.0);
        network.getPostActivation()[0] = 1.0;
    }

    @Override
    public AgentController clone() {
        NEATNetwork cloneNet = new NEATNetwork(network.getInputCount(), 
                network.getOutputCount(), Arrays.asList(network.getLinks()), 
                network.getActivationFunctions());
        NEATAgentController cloneAC = new NEATAgentController(cloneNet);
        return cloneAC;
    }
}
