/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.controllers.MultiAgentControllerIndividual;

/**
 *
 * @author jorge
 */
public class MultiIndividual extends DoubleVectorIndividual implements MultiAgentControllerIndividual {


    public final static String P_NUM_AGENTS = "num-agents"; 
    private static final long serialVersionUID = 1L;
    protected int numAgents;
    protected int length;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();
        
        this.numAgents = state.parameters.getInt(def.push(P_NUM_AGENTS), null);
        if(this.numAgents < 1 ) {
            state.output.fatal("Invalid number of agents: " + numAgents, def.push(P_NUM_AGENTS));
        }

        int l = ((FloatVectorSpecies) species).genomeSize;
        if(l % numAgents != 0) {
            state.output.fatal("Genome size " + l + " is not a multiple of the number of agents " + numAgents);
        }
        length = l / numAgents;
    }


    @Override
    public AgentController[] decodeControllers() {
        AgentController[] agents = new AgentController[numAgents];
        for(int i = 0 ; i < agents.length ; i++) {
            double[] subGenome = Arrays.copyOfRange(genome, i * length, i * length + length);
            agents[i] = new AllocationAgent(subGenome);
        }
        return agents;
    }
    
}
