/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.AgentController;
import mase.AgentControllerIndividual;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.ga.core.Chromosome;

/**
 * NEATGenome wrapper
 *
 * @author jorge
 */
public class NEATIndividual extends Individual implements AgentControllerIndividual {

    private Chromosome genome;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
    }

    public void setChromosome(Chromosome genome) {
        this.genome = genome;
    }

    public Chromosome getChromosome() {
        return genome;
    }

    @Override
    public boolean equals(Object ind) {
        return genome.equals(genome);
    }

    @Override
    public int hashCode() {
        return genome.hashCode();
    }

    @Override
    public Parameter defaultBase() {
        return defaultBase();
    }

    @Override
    public AgentController decodeController() {
        NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(genome);
        NEATNeuralNet net = new NEATNeuralNet();
        net.createNetStructure(descr);
        net.updateNetStructure();
        NEATAgentController nac = new NEATAgentController(net);
        return nac;
    }
}
