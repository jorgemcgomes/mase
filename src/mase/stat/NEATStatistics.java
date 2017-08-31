/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.neat.NEATGene;
import ec.neat.NEATIndividual;
import ec.neat.NEATNetwork;
import ec.neat.NEATNode;
import ec.neat.NEATSpecies;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        state.output.println("Generation Subpop Species Avg.neurons Avg.links Avg.recurr Avg.disabled Best.neurons Best.links Best.recurr Best.disabled", log);
    }
    

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        // Gen | Subpop | Species | Avg neurons | Avg links | Avg recurr | Best neurons | Best links | Best recurr
        for (int i = 0; i < state.population.subpops.size(); i++) {
            if(state.population.subpops.get(i).species instanceof NEATSpecies) {
                double highestFitness = Double.NEGATIVE_INFINITY;
                int[] bestDescr = null;
                double[] avgDescr = new double[4];
                for (int j = 0; j < state.population.subpops.get(i).individuals.size(); j++) {
                    NEATIndividual ind = (NEATIndividual) state.population.subpops.get(i).individuals.get(j);
                    int[] descr = netDescription(ind.createNetwork());
                    if(ind.fitness.fitness() > highestFitness) {
                        highestFitness = ind.fitness.fitness();
                        bestDescr = descr;
                    }
                    avgDescr[0] += descr[0];
                    avgDescr[1] += descr[1];
                    avgDescr[2] += descr[2];
                    avgDescr[3] += descr[3];
                }
                avgDescr[0] /= (double) state.population.subpops.get(i).individuals.size();
                avgDescr[1] /= (double) state.population.subpops.get(i).individuals.size();
                avgDescr[2] /= (double) state.population.subpops.get(i).individuals.size();
                avgDescr[3] /= (double) state.population.subpops.get(i).individuals.size();
                
                NEATSpecies species = (NEATSpecies) state.population.subpops.get(i).species;
                state.output.println(state.generation + " " + i + " " + species.subspecies.size() + " " + 
                        avgDescr[0] + " " + avgDescr[1] + " " + avgDescr[2] + " " + avgDescr[3] + " " +
                        bestDescr[0] + " " + bestDescr[1] + " " + bestDescr[2] + " " + bestDescr[3], log);
            }
        }
    }

    private int[] netDescription(NEATNetwork net) {
        int[] res = new int[4];
        res[0] = net.nodes.size(); // neurons
        int totalConnections = 0, recursive = 0, disabled = 0;
        for(NEATNode node : net.nodes) {
            ArrayList<NEATGene> conns = node.incomingGenes;
            for(NEATGene g : conns) {
                totalConnections++;
                if(g.isRecurrent) {
                    recursive++;
                }
                if(!g.enable) {
                    disabled++;
                }
            }
        }
        res[1] = totalConnections;
        res[2] = recursive;
        res[3] = disabled;

        return res;
    }    
    
}
