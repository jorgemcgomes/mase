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
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;

/**
 * NEATGenome wrapper
 *
 * @author jorge
 */
public class NEATIndividual extends Individual implements AgentControllerIndividual {

    private NEATCODEC codec;
    private NEATGenome genome;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.codec = new NEATCODEC();
    }
    
    public void setGenome(NEATGenome genome) {
        this.genome = genome;
    }
    
    public NEATGenome getGenome() {
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
        NEATNetwork net = (NEATNetwork) codec.decode(genome);
        NEATAgentController ac = new NEATAgentController(net);
        return ac;
    }
}
