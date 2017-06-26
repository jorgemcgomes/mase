/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import java.util.Arrays;
import static mase.controllers.NeuralControllerIndividual.P_HIDDEN;
import static mase.controllers.NeuralControllerIndividual.P_INPUTS;
import static mase.controllers.NeuralControllerIndividual.P_OUTPUTS;
import static mase.controllers.NeuralControllerIndividual.P_STRUCTURE;
import static mase.controllers.NeuralControllerIndividual.P_TANH;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.NeuralNetworkPattern;

/**
 *
 * @author jorge
 */
public class MultiNeuralControllerIndividual extends DoubleVectorIndividual implements MultiAgentControllerIndividual {

    public final static String P_NUM_AGENTS = "num-agents"; 
    private static final long serialVersionUID = 1L;
    protected int numAgents;
    protected NeuralNetworkPattern[] prototypePatterns;
    protected int[] begins;
    protected int[] lengths;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();
        
        this.numAgents = state.parameters.getInt(def.push(P_NUM_AGENTS), null);
        if(this.numAgents < 1 ) {
            state.output.fatal("Invalid number of agents: " + numAgents, def.push(P_NUM_AGENTS));
        }
        
        prototypePatterns = new NeuralNetworkPattern[numAgents];
        begins = new int[numAgents];
        lengths = new int[numAgents];
        int totalSize = 0;
        
        for(int i = 0 ; i < numAgents ; i++) {
            // Create network
            String structure = state.parameters.getString(def.push(P_STRUCTURE).push(""+i), def.push(P_STRUCTURE));
            int input = state.parameters.getInt(def.push(P_INPUTS).push(""+i), def.push(P_INPUTS));
            int hidden = state.parameters.getInt(def.push(P_HIDDEN).push(""+i), def.push(P_HIDDEN));
            int output = state.parameters.getInt(def.push(P_OUTPUTS).push(""+i), def.push(P_OUTPUTS));
            boolean tanh = state.parameters.getBoolean(def.push(P_TANH).push(""+i), def.push(P_TANH), false);
            prototypePatterns[i] = NeuralControllerIndividual.createNetworkPattern(structure, input, hidden, output, tanh);
            
            BasicNetwork patternTest = (BasicNetwork) prototypePatterns[i].generate();
            int size = patternTest.getStructure().calculateSize();
            lengths[i] = size;
            begins[i] = totalSize;
            totalSize += size;
        }

        if (((FloatVectorSpecies) species).genomeSize != totalSize) {
            state.output.fatal("NN weights (" + totalSize + ":" + Arrays.toString(lengths) + 
                    ") does not match genome size (" + ((FloatVectorSpecies) species).genomeSize + ").");
        }
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(NeuralControllerIndividual.DEFAULT_BASE);
    }

    @Override
    public AgentController[] decodeControllers() {
        AgentController[] agents = new AgentController[numAgents];
        for(int i = 0 ; i < agents.length ; i++) {
            BasicNetwork subNetwork = (BasicNetwork) prototypePatterns[i].generate();
            double[] subGenome = Arrays.copyOfRange(genome, begins[i], begins[i] + lengths[i]);
            subNetwork.decodeFromArray(subGenome);
            agents[i] = new NeuralAgentController(subNetwork.getFlat());
        }
        return agents;
    }
    
}
