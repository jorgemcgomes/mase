/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.evaluation.ExpandedFitness;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.nn.core.Synapse;

/**
 *
 * @author jorge
 */
public class NEATStatistics extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(
                base.push(P_FILE), null);
        if (statisticsFile != null) {
            try {
                log = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        state.output.println("Generation Subpop Species Avg.neurons Avg.links Avg.recurr Best.neurons Best.links Best.recurr", log);
    }
    

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        // Gen | Subpop | Species | Avg neurons | Avg links | Avg recurr | Best neurons | Best links | Best recurr
        for (int i = 0; i < state.population.subpops.size(); i++) {
            if(state.population.subpops.get(i) instanceof NEATSubpop) {
                NEATGeneticAlgorithm neat = ((NEATSubpop) state.population.subpops.get(i)).getNEAT();
                double highestFitness = Double.NEGATIVE_INFINITY;
                int[] bestDescr = null;
                double[] avgDescr = new double[3];
                for (int j = 0; j < state.population.subpops.get(i).individuals.size(); j++) {
                    NEATIndividual ind = (NEATIndividual) state.population.subpops.get(i).individuals.get(j);
                    int[] descr = netDescription(ind.getNeuralNet());
                    if(((ExpandedFitness) ind.fitness).getFitnessScore() > highestFitness) {
                        highestFitness = ind.fitness.fitness();
                        bestDescr = descr;
                    }
                    avgDescr[0] += descr[0];
                    avgDescr[1] += descr[1];
                    avgDescr[2] += descr[2];
                }
                avgDescr[0] /= state.population.subpops.get(i).individuals.size();
                avgDescr[1] /= state.population.subpops.get(i).individuals.size();
                avgDescr[2] /= state.population.subpops.get(i).individuals.size();
                state.output.println(state.generation + " " + i + " " + neat.getSpecies().specieList().size() + " " + 
                        avgDescr[0] + " " + avgDescr[1] + " " + avgDescr[2] + " " +
                        bestDescr[0] + " " + bestDescr[1] + " " + bestDescr[2], log);
            }
        }
    }

    private int[] netDescription(NEATNeuralNet net) {
        net.updateNetStructure();
        int[] res = new int[3];
        res[0] = net.neurons().length;
        res[1] = net.connections().length;
        res[2] = 0;
        for(Synapse s : net.connections()) {
            if(((NEATNeuron) s.getFrom()).id() == ((NEATNeuron) s.getTo()).id() || // self-recurrent
                    ((NEATNeuron) s.getFrom()).neuronDepth() < ((NEATNeuron) s.getTo()).neuronDepth()) { // recurrent
                res[2]++;
            }
        }
        return res;
    }
}
