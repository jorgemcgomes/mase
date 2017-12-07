/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import java.util.Arrays;
import mase.controllers.AgentController;
import mase.controllers.EncodableAgentController;
import mase.util.FormatUtils;
import org.neat4j.neat.core.NEATFeatureGene;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.core.NEATNodeGene;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.data.csv.CSVInput;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.nn.core.Synapse;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements EncodableAgentController {

    private static final long serialVersionUID = 1;
    private NEATNeuralNet network;
    private double[] extraGenes;
    
    public NEATAgentController() {
        this.network = null;
    }

    public NEATAgentController(NEATNeuralNet network) {
        this.network = network;
        this.extraGenes = extractExtraGenes(network);
    }
    
    public NEATNeuralNet getNetwork() {
        return network;
    }
    
    public double[] getExtraGenes() {
        return extraGenes;
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
        int in = 0, out = 0;
        for (NEATNeuron n : network.neurons()) {
            if(n.neuronType() == NEATNodeGene.INPUT) {
                in++;
            } else if(n.neuronType() == NEATNodeGene.OUTPUT) {
                out++;
            }
        }
        return "{In:"+in + " Out:"+out + " Neurons:" + network.neurons().length + " Links:" + network.connections().length
                + " SelfRec:" + selfRecurr + " Rec:" + recurr + " Extra:" + extraGenes.length + "}";/* + "\n\n"
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
        this.extraGenes = extractExtraGenes(net);
    }
    
    private static double[] extractExtraGenes(NEATNeuralNet net) {
        Chromosome chromo = ((NEATNetDescriptor) net.netDescriptor()).neatStructure();
        double[] extraFeatures = new double[chromo.genes().length];
        int index = 0;
        for(Gene g : chromo.genes()) {
            if(g instanceof NEATFeatureGene) {
                extraFeatures[index++] = g.geneAsNumber().doubleValue();
            }            
        }
        return Arrays.copyOf(extraFeatures, index);
    }

}
