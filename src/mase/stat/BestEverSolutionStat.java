/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.File;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestEverSolutionStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    public static final String P_LAST_FILE = "last";
    File bestFile;
    File lastFile;
    double bestSoFar;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File f = state.parameters.getFile(base.push(P_FILE), null);
        bestFile = new File(f.getParent(), prefix + f.getName());
        bestSoFar = Double.NEGATIVE_INFINITY;
        f = state.parameters.getFile(base.push(P_LAST_FILE), null);
        lastFile = new File(f.getParent(), prefix + f.getName());
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        double bestFitness = Double.NEGATIVE_INFINITY;
        Individual best = null;
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                float fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > bestFitness) {
                    bestFitness = fit;
                    best = ind;
                }
            }
        }

        if (bestFitness > bestSoFar) {
            bestSoFar = bestFitness;
            super.writeSolution(best, bestFile);
        }
        
        super.writeSolution(best, lastFile);
        
    }
}
