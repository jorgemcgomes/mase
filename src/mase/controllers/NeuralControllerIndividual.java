 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.pattern.JordanPattern;
import org.encog.neural.pattern.NeuralNetworkPattern;

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
    private BasicNetwork prototypeNetwork;

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
        prototypeNetwork = createPrototypeNetwork(structure, input, hidden, output, tanh);

        int genomeSize = ((FloatVectorSpecies) species).genomeSize;
        if (genomeSize != prototypeNetwork.getStructure().calculateSize()) {
            state.output.fatal("NN weights (" + prototypeNetwork.getStructure().calculateSize() + ") does not match genome size (" + genomeSize + ").");
        }
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(DEFAULT_BASE);
    }

    @Override
    public AgentController decodeController() {
        BasicNetwork network = (BasicNetwork) prototypeNetwork.clone();
        network.decodeFromArray(genome);
        return new NeuralAgentController(network);
    }
    
    protected static BasicNetwork createPrototypeNetwork(String structure, int in, int hidden, int out, boolean tanh) {
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
        pattern.addHiddenLayer(hidden);
        pattern.setOutputNeurons(out);
        return (BasicNetwork) pattern.generate();
    }
}
