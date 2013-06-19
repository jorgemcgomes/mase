/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import java.util.Arrays;
import mase.AgentController;
import mase.AgentControllerIndividual;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.flat.FlatLayer;
import org.encog.neural.flat.FlatNetwork;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NeuralControllerIndividual extends DoubleVectorIndividual implements AgentControllerIndividual {

    public static final String DEFAULT_BASE = "neural";
    public static final String P_INPUTS = "input";
    public static final String P_HIDDEN = "hidden";
    public static final String P_OUTPUTS = "output";
    public static final String P_STRUCTURE = "structure";
    public static final String P_TANH = "tanh";
    public static final String FEED_FORWARD = "feed-forward";
    public static final String ELMAN = "elman";
    public static final String JORDAN = "jordan";
    private FlatNetwork prototypeNetwork;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        
        Parameter def = defaultBase();

        // Create network
        String structure = state.parameters.getStringWithDefault(base.push(P_STRUCTURE), def.push(P_STRUCTURE), FEED_FORWARD);
        int input = state.parameters.getInt(base.push(P_INPUTS), def.push(P_INPUTS));
        int hidden = state.parameters.getInt(base.push(P_HIDDEN), def.push(P_HIDDEN));
        int output = state.parameters.getInt(base.push(P_OUTPUTS), def.push(P_OUTPUTS));
        boolean tanh = state.parameters.getBoolean(base.push(P_TANH), def.push(P_TANH), false);
        // TODO: check for parameter errors

        final ActivationFunction linearAct = new ActivationLinear();
        final ActivationFunction act = tanh ? new ActivationTANH() : new ActivationSigmoid();
        FlatLayer[] layers;
        if (hidden == 0) {
            layers = new FlatLayer[2];
            layers[0] = new FlatLayer(linearAct, input, FlatNetwork.DEFAULT_BIAS_ACTIVATION);
            layers[1] = new FlatLayer(act, output, FlatNetwork.NO_BIAS_ACTIVATION);
        } else {
            layers = new FlatLayer[3];
            layers[0] = new FlatLayer(linearAct, input, FlatNetwork.DEFAULT_BIAS_ACTIVATION);
            layers[1] = new FlatLayer(act, hidden, FlatNetwork.DEFAULT_BIAS_ACTIVATION);
            layers[2] = new FlatLayer(act, output, FlatNetwork.NO_BIAS_ACTIVATION);
        }
        if (hidden > 0) {
            if (structure.equalsIgnoreCase(ELMAN)) {
                layers[0].setContextFedBy(layers[1]);
            } else if (structure.equalsIgnoreCase(JORDAN)) {
                layers[0].setContextFedBy(layers[2]);
            } else if (!structure.equalsIgnoreCase(FEED_FORWARD)) {
                state.output.fatal("Unknown structure: " + structure, base.push(P_STRUCTURE));
            }
        }

        prototypeNetwork = new FlatNetwork(layers);
        int genomeSize = ((FloatVectorSpecies) species).genomeSize;
        if (genomeSize != prototypeNetwork.getWeights().length) {
            state.output.fatal("NN weights (" + prototypeNetwork.getWeights().length + ") does not match genome size (" + genomeSize + ").");
        }
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(DEFAULT_BASE);
    }

    @Override
    public AgentController decodeController() {
        FlatNetwork network = prototypeNetwork.clone();
        network.decodeNetwork(genome);
        return new NeuralAgentController(network);
    }

    public static class NeuralAgentController implements AgentController {

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

        @Override
        public AgentController clone() {
            return new NeuralAgentController(network.clone());
        }
    }
}
