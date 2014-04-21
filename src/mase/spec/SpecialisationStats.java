/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
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

        for (int i = 0; i < exc.prototypeSubs.length; i++) {
            // MetaPop to which they belong
            MetaPopulation pop = null;
            for (int m = 0; m < exc.metaPops.size(); m++) {
                if (exc.metaPops.get(m).populations.contains(i)) {
                    pop = exc.metaPops.get(m);
                    state.output.print(" " + m, log);
                }
            }

            // Population dispersion
            state.output.print(" " + exc.originalMatrix[i][i], log);

            // Normalised distance to internal pops -- include itself -- 1
            ds.clear();
            for(Integer p : pop.populations) {
                ds.addValue(exc.distanceMatrix[i][p]);
            }
            state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);
            
            // Normalised distance to external pops
            ds.clear();
            for (MetaPopulation mp : exc.metaPops) {
                if (mp != pop) {
                    for(Integer p : mp.populations) {
                        ds.addValue(exc.distanceMatrix[i][p]);
                    }
                }
            }
            if(ds.getN() == 0) {
                ds.addValue(1);
            }
            state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);
        }

        String str = "";
        for (MetaPopulation mp : exc.metaPops) {
            str += mp + " ; ";
        }
        state.output.message(str);

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
