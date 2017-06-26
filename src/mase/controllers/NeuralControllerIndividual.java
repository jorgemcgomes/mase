/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import java.util.List;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.MLMethod;
import org.encog.neural.flat.FlatLayer;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.layers.Layer;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.pattern.JordanPattern;
import org.encog.neural.pattern.NeuralNetworkPattern;
import org.encog.neural.pattern.PatternError;

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
    private static final long serialVersionUID = 1L;
    private NeuralNetworkPattern prototypePattern;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();

        // Create network
        String structure = state.parameters.getString(base.push(P_STRUCTURE), def.push(P_STRUCTURE));
        int input = state.parameters.getInt(base.push(P_INPUTS), def.push(P_INPUTS));
        int hidden = state.parameters.getInt(base.push(P_HIDDEN), def.push(P_HIDDEN));
        int output = state.parameters.getInt(base.push(P_OUTPUTS), def.push(P_OUTPUTS));
        boolean tanh = state.parameters.getBoolean(base.push(P_TANH), def.push(P_TANH), false);
        prototypePattern = createNetworkPattern(structure, input, hidden, output, tanh);

        BasicNetwork patternTest = (BasicNetwork) prototypePattern.generate();

        int genomeSize = ((FloatVectorSpecies) species).genomeSize;
        if (genomeSize != patternTest.getStructure().calculateSize()) {
            state.output.fatal("NN weights (" + patternTest.getStructure().calculateSize() + ") does not match genome size (" + genomeSize + ").");
        }
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(DEFAULT_BASE);
    }

    @Override
    public AgentController decodeController() {
        BasicNetwork network = (BasicNetwork) prototypePattern.generate();
        network.decodeFromArray(genome);
        return new NeuralAgentController(network.getFlat());
    }

    protected static NeuralNetworkPattern createNetworkPattern(String structure, int in, int hidden, int out, boolean tanh) {
        ActivationFunction act = tanh ? new ActivationTANH() : new ActivationSigmoid();
        NeuralNetworkPattern pattern = null;
        if (structure.equalsIgnoreCase(ELMAN)) {
            pattern = new ElmanPattern2();
        } else if (structure.equalsIgnoreCase(JORDAN)) {
            pattern = new JordanPattern();
        } else if (structure.equalsIgnoreCase(FEED_FORWARD)) {
            pattern = new FeedForwardPattern();
        } else {
            return null;
        }
        pattern.setActivationFunction(act);
        pattern.setInputNeurons(in);
        if (hidden > 0) {
            pattern.addHiddenLayer(hidden);
        }
        pattern.setOutputNeurons(out);
        return pattern;
    }

    public static class ElmanPattern2 implements NeuralNetworkPattern {

        private int inputNeurons;
        private int outputNeurons;
        private int hiddenNeurons;
        private ActivationFunction activation;

        public ElmanPattern2() {
            this.inputNeurons = -1;
            this.outputNeurons = -1;
            this.hiddenNeurons = -1;
        }

        /**
         * Add a hidden layer with the specified number of neurons.
         *
         * @param count The number of neurons in this hidden layer.
         */
        @Override
        public void addHiddenLayer(final int count) {
            if (this.hiddenNeurons != -1) {
                throw new PatternError(
                        "An Elman neural network should have only one hidden layer.");
            }
            this.hiddenNeurons = count;
        }

        /**
         * Clear out any hidden neurons.
         */
        @Override
        public void clear() {
            this.hiddenNeurons = -1;
        }

        /**
         * Generate the Elman neural network.
         *
         * @return The Elman neural network.
         */
        @Override
        public MLMethod generate() {
            BasicLayer hidden, input;

            final BasicNetwork network = new BasicNetwork();
            network.addLayer(input = new BasicLayer(null, true,
                    this.inputNeurons));
            network.addLayer(hidden = new BasicLayer(this.activation, true,
                    this.hiddenNeurons));
            network.addLayer(new BasicLayer(this.activation, false, this.outputNeurons));
            input.setContextFedBy(hidden);
            network.getStructure().finalizeStructure();
            network.reset();
            return network;
        }

        /**
         * Set the activation function to use on each of the layers.
         *
         * @param activation The activation function.
         */
        @Override
        public void setActivationFunction(final ActivationFunction activation) {
            this.activation = activation;
        }

        /**
         * Set the number of input neurons.
         *
         * @param count Neuron count.
         */
        @Override
        public void setInputNeurons(final int count) {
            this.inputNeurons = count;
        }

        /**
         * Set the number of output neurons.
         *
         * @param count Neuron count.
         */
        @Override
        public void setOutputNeurons(final int count) {
            this.outputNeurons = count;
        }
    }
}
