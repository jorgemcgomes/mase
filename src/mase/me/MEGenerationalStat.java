/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge
 */
public class MEGenerationalStat extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int log;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File file = state.parameters.getFile(base.push(P_FILE), null);
        try {
            log = state.output.addLog(file, true);
        } catch (IOException ex) {
            state.output.fatal("An IOException occurred while trying to create the log " + file);
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        state.output.println("Generation FilledBins TotalSize MinFitness MeanFitness MaxFitness New", log);
    }

    @Override
    public void postBreedingStatistics(EvolutionState state) {
        super.postBreedingStatistics(state);
        MESubpopulation pop = (MESubpopulation) state.population.subpops.get(0);
        DescriptiveStatistics fit = new DescriptiveStatistics();
        for (Individual ind : pop.map.values()) {
            fit.addValue(((ExpandedFitness) ind.fitness).getFitnessScore());
        }
        state.output.println(state.generation + " " + pop.map.keySet().size() + " "
                + pop.map.size() + " " + fit.getMin() + " " + fit.getMean() + " "
                + fit.getMax() + " " + pop.newInRepo, log);
        state.output.message("Repertoire size: " + pop.map.keySet().size() + " | New: " + 
                pop.newInRepo + " | Avg. fitness: " + new DecimalFormat("0.0000").format(fit.getMean()));
    }

}
