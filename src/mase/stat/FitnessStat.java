/*
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package mase.stat;

import ec.*;
import ec.util.*;
import java.io.*;
import mase.MaseProblem;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class FitnessStat extends Statistics {

    public static final String P_DO_SUBPOPS = "do-subpops";
    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int statisticslog = 0;  // stdout by default
    public boolean doSubpops;
    public double[] bestSoFar;
    public double absoluteBest;
    public File statisticsFile;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);

        if (statisticsFile != null) {
            try {
                statisticslog = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS), null, false);
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        // header
        state.output.println("Generation Evaluations Subpop Individuals MinFitness MeanFitness MaxFitness BestSoFar", statisticslog);
    }

    @Override
    public void postInitializationStatistics(final EvolutionState state) {
        super.postInitializationStatistics(state);
        // set up our bestSoFar array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        bestSoFar = new double[state.population.subpops.size()];
    }

    /**
     * Prints out the statistics, but does not end with a println -- this lets
     * overriding methods print additional statistics on the same line
     */
    @Override
    public void postEvaluationStatistics(final EvolutionState state) {
        super.postEvaluationStatistics(state);

        int subpops = state.population.subpops.size();                          // number of supopulations
        DescriptiveStatistics[] fitness = new DescriptiveStatistics[subpops];
        for (int i = 0; i < subpops; i++) {
            fitness[i] = new DescriptiveStatistics();
        }

        // gather per-subpopulation statistics
        for (int x = 0; x < subpops; x++) {
            for (Individual ind : state.population.subpops.get(x).individuals) {
                if (ind.evaluated) {// he's got a valid fitness
                    // update fitness
                    double f = ((ExpandedFitness) ind.fitness).getFitnessScore();
                    bestSoFar[x] = Math.max(bestSoFar[x], f);
                    absoluteBest = Math.max(absoluteBest, f);
                    fitness[x].addValue(f);
                }
            }
            // print out fitness information
            if (doSubpops) {
                state.output.println(state.generation + " " + state.evaluations + " " + x + " "
                        + fitness[x].getN() + " " + fitness[x].getMin()
                        + " " + fitness[x].getMean() + " " + fitness[x].getMax() + " "
                        + bestSoFar[x], statisticslog);
            }
        }

        // Now gather global statistics
        DescriptiveStatistics global = new DescriptiveStatistics();
        for (DescriptiveStatistics ds : fitness) {
            for (double v : ds.getValues()) {
                global.addValue(v);
            }
        }

        state.output.println(state.generation + " " + state.evaluations + " NA "
                + global.getN() + " " + global.getMin()
                + " " + global.getMean() + " " + global.getMax() + " "
                + absoluteBest, statisticslog);
    }
}
