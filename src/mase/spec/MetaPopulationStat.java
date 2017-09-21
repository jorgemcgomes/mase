/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.evaluation.MetaEvaluator;
import mase.neat.NEATIndividual;
import mase.spec.AbstractHybridExchanger.MetaPopulation;
import org.neat4j.neat.core.NEATSpecieManager;

/**
 *
 * @author jorge
 */
public class MetaPopulationStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //To change body of generated methods, choose Tools | Templates.
        File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);
        if (statisticsFile != null) {
            try {
                log = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
    }

    @Override
    public void postPreBreedingExchangeStatistics(EvolutionState state) {
        super.postPreBreedingExchangeStatistics(state);
        AbstractHybridExchanger exc = (AbstractHybridExchanger) state.exchanger;
        StochasticHybridExchanger she = (StochasticHybridExchanger) exc;
        MetaEvaluator me = (MetaEvaluator) state.evaluator;

        for (int i = 0; i < exc.metaPops.size(); i++) {
            MetaPopulation mp = exc.metaPops.get(i);
            state.output.print(state.generation + " " + me.totalEvaluations + " " + i + " " + mp.origin + " " + mp.agents.size() + " " + mp.age + " " + mp.lockDown, log);

            // silhouette distances
            for (int j = 0; j < exc.nAgents; j++) {
                if (she.distanceMatrix == null || she.distanceMatrix.length != exc.metaPops.size() || j >= she.distanceMatrix.length) {
                    state.output.print(" NA", log);
                } else {
                    if (Double.isNaN(she.distanceMatrix[i][j])) {
                        state.output.print(" NA", log);
                    } else {
                        state.output.print(" " + she.distanceMatrix[i][j], log);
                    }
                }
            }

            // genetic distances
            for (int j = 0; j < exc.nAgents; j++) {
                if (j >= exc.metaPops.size()) {
                    state.output.print(" NA", log);
                } else {
                    double d = geneticDistance(mp, exc.metaPops.get(j));
                    state.output.print(" " + d, log);
                }
            }

            state.output.println("", log);
        }

    }

    private double geneticDistance(MetaPopulation mp1, MetaPopulation mp2) {
        double dist = 0;
        int c = 0;
        Individual[] inds1 = mp1.pop.individuals;
        Individual[] inds2 = mp2.pop.individuals;

        for (Individual i1 : inds1) {
            for (Individual i2 : inds2) {
                NEATIndividual n1 = (NEATIndividual) i1;
                NEATIndividual n2 = (NEATIndividual) i2;
                if(n1 != n2) {
                    dist += NEATSpecieManager.specieManager().compatibilityScore(n1.getChromosome(), n2.getChromosome(), 1, 1, 0.4);
                    c++;
                }
            }
        }

        return dist / c;

    }

}
