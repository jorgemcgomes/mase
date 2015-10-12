/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestEverSolutionStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    protected File bestFile;
    protected double bestSoFar;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File f = state.parameters.getFile(base.push(P_FILE), null);
        bestFile = new File(f.getParent(), jobPrefix + f.getName());
        bestSoFar = Double.NEGATIVE_INFINITY;
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        double bestFitness = Double.NEGATIVE_INFINITY;
        Individual best = null;
        int sub = -1;
        int index = -1;
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                Individual ind = state.population.subpops[i].individuals[j];
                double fit = ((ExpandedFitness) ind.fitness).getFitnessScore();
                if (fit > bestFitness) {
                    bestFitness = fit;
                    best = ind;
                    sub = i;
                    index = j;
                }
            }
        }

        if (bestFitness > bestSoFar) {
            bestSoFar = bestFitness;
            PersistentSolution c = SolutionPersistence.createPersistentController(state, best, sub, index);
            try {
                SolutionPersistence.writeSolution(c, bestFile);
            } catch (IOException ex) {
                Logger.getLogger(BestEverSolutionStat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
}
