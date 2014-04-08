/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.coevolve.MultiPopCoevolutionaryEvaluator2;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import mase.MetaEvaluator;
import mase.spec.SpecialisationExchanger.MetaPopulation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SpecialisationStats extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
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
        SpecialisationExchanger exc = (SpecialisationExchanger) state.exchanger;
        state.output.print(state.generation + " " + exc.metaPops.size(), log);

        // metapop size (min, mean, max)
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.populations.size());
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // metapop dispersion (min, mean, max)
        ds.clear();
        for (MetaPopulation mp : exc.metaPops) {
            double dispersion = 0;
            for (Integer i : mp.populations) {
                for (Integer j : mp.populations) {
                    dispersion += exc.distanceMatrix[i][j];
                }
            }
            ds.addValue(dispersion / (mp.populations.size() * mp.populations.size()));
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // total number of merges and splits
        int count = 0;
        for (MetaPopulation mp : exc.metaPops) {
            count += mp.waitingIndividuals.size();
        }
        state.output.print(" " + count + " " + exc.splits, log);

        // population distance to others (min, mean, max)
        for (int i = 0; i < exc.subpopN; i++) {
            ds.clear();
            for (int j = 0; j < exc.subpopN; j++) {
                if (j != i) {
                    ds.addValue(exc.distanceMatrix[i][j]);
                }
            }
            state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);
        }

        // the metapop to each they belong
        for (int i = 0; i < exc.subpopN; i++) {
            for (int m = 0; m < exc.metaPops.size(); m++) {
                if (exc.metaPops.get(m).populations.contains(i)) {
                    state.output.print(" " + m, log);
                }
            }
        }
        
        /*for(double[] m : exc.distanceMatrix) {
            state.output.message(Arrays.toString(m));
        }*/
        
        // representatives
        /*MetaEvaluator me = (MetaEvaluator) state.evaluator;
        MultiPopCoevolutionaryEvaluator2 baseEval = (MultiPopCoevolutionaryEvaluator2) me.getBaseEvaluator();
        Individual[][] elites = baseEval.getEliteIndividuals();
        ds.clear();
        for(MetaPopulation mp : exc.metaPops) {
            HashSet<Individual> inds = new HashSet<Individual>();
            for(Integer p : mp.populations) {
                inds.add(elites[p][0]);
            }
            ds.addValue(inds.size() / (double) mp.populations.size());
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);*/
        
        
        state.output.println("", log);
    }

}
