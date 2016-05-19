/*
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package mase.stat;

import ec.*;
import mase.evaluation.CoevolutionaryEvaluator;
import ec.util.*;
import java.io.*;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.ExpandedFitness;

/* 
 * SimpleShortStatistics.java
 * 
 * Created: Tue Jun 19 15:08:29 EDT 2001
 * By: Sean Luke
 */
/**
 * A Simple-style statistics generator, intended to be easily parseable with awk
 * or other Unix tools. Prints fitness information, one generation (or
 * pseudo-generation) per line. If do-time is true, then timing information is
 * also given. If do-size is true, then size information is also given. No final
 * statistics information is provided. You can also set SimpleShortStatistics to
 * only output every *modulus* generations to keep the tally shorter. And you
 * can gzip the statistics file.
 *
 * <p> Each line represents a single generation. The first items on a line are
 * always:
 * <ul>
 * <li> The generation number
 * <li> (if do-time) how long initialization took in milliseconds, or how long
 * the previous generation took to breed to form this generation
 * <li> (if do-time) How long evaluation took in milliseconds this generation
 * </ul>
 *
 * <p>Then, (if do-subpops) the following items appear, once per each
 * subpopulation:
 * <ul>
 * <li> (if do-size) The average size of an individual this generation
 * <li> (if do-size) The average size of an individual so far in the run
 * <li> (if do-size) The size of the best individual this generation
 * <li> (if do-size) The size of the best individual so far in the run
 * <li> The mean fitness of the subpopulation this generation
 * <li> The best fitness of the subpopulation this generation
 * <li> The best fitness of the subpopulation so far in the run
 * </ul>
 *
 * <p>Then the following items appear, for the whole population:
 * <ul>
 * <li> (if do-size) The average size of an individual this generation
 * <li> (if do-size) The average size of an individual so far in the run
 * <li> (if do-size) The size of the best individual this generation
 * <li> (if do-size) The size of the best individual so far in the run
 * <li> The mean fitness this generation
 * <li> The best fitness this generation
 * <li> The best fitness so far in the run
 * </ul>
 *
 *
 * Compressed files will be overridden on restart from checkpoint; uncompressed
 * files will be appended on restart.
 *
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>file</tt><br>
 * <font size=-1>String (a filename), or nonexistant (signifies
 * stdout)</font></td>
 * <td valign=top>(the log for statistics)</td></tr>
 * <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 * <font size=-1>boolean</font></td>
 * <td valign=top>(whether or not to compress the file (.gz suffix
 * added)</td></tr>
 * <tr><td valign=top><i>base.</i><tt>modulus</tt><br>
 * <font size=-1>integer >= 1 (default)</font></td>
 * <td valign=top>(How often (in generations) should we print a statistics
 * line?)</td></tr>
 * <tr><td valign=top><i>base</i>.<tt>do-time</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(print timing information?)</td></tr>
 * <tr><td valign=top><i>base</i>.<tt>do-size</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(print sizing information?)</td></tr>
 * <tr><td valign=top><i>base</i>.<tt>do-subpops</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(print information on a per-subpop basis as well as
 * per-population?)</td></tr>
 * </table>
 *
 * @author Sean Luke
 * @version 2.0
 */
public class FitnessStat extends Statistics {

    public static final String P_DO_SUBPOPS = "do-subpops";
    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int statisticslog = 0;  // stdout by default
    public boolean doSubpops;
    public Individual[] bestSoFar;
    public Individual absoluteBest;
    public long[] totalIndsSoFar;
    public long[] totalIndsThisGen;                         // total assessed individuals
    public double[] totalFitnessThisGen;                    // per-subpop mean fitness this generation
    public Individual[] bestOfGeneration;   // per-subpop best individual this generation
    public File statisticsFile;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        statisticsFile = state.parameters.getFile(
                base.push(P_STATISTICS_FILE), null);

        if (statisticsFile != null) {
            try {
                statisticslog = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS), null, false);
    }

    public Individual[] getBestSoFar() {
        return bestSoFar;
    }

    @Override
    public void postInitializationStatistics(final EvolutionState state) {
        super.postInitializationStatistics(state);
        // set up our bestSoFar array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        bestSoFar = new Individual[state.population.subpops.length];
        // print out our generation number
        state.output.print("0 0 ", statisticslog); // MODIFIED FOR EVALUATIONS LIMIT

        totalIndsSoFar = new long[state.population.subpops.length];
    }

    @Override
    public void postBreedingStatistics(final EvolutionState state) {
        super.postBreedingStatistics(state);
        int evals = ((MetaEvaluator) state.evaluator).totalEvaluations;
        state.output.print((state.generation + 1) + " " + evals + " ", statisticslog); // 1 because we're putting the breeding info on the same line as the generation it *produces*, and the generation number is increased *after* breeding occurs, and statistics for it
    }

    /**
     * Prints out the statistics, but does not end with a println -- this lets
     * overriding methods print additional statistics on the same line
     */
    @Override
    public void postEvaluationStatistics(final EvolutionState state) {
        super.postEvaluationStatistics(state);

        int subpops = state.population.subpops.length;                          // number of supopulations
        totalIndsThisGen = new long[subpops];                                           // total assessed individuals
        bestOfGeneration = new Individual[subpops];                                     // per-subpop best individual this generation
        totalFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation
        double[] meanFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation

        // gather per-subpopulation statistics
        for (int x = 0; x < subpops; x++) {
            for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
                if (state.population.subpops[x].individuals[y].evaluated) {// he's got a valid fitness
                    // update sizes
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;

                    // update fitness
                    if (bestOfGeneration[x] == null
                            || ((ExpandedFitness) state.population.subpops[x].individuals[y].fitness).getFitnessScore()
                            > ((ExpandedFitness) bestOfGeneration[x].fitness).getFitnessScore()) {
                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                        if (bestSoFar[x] == null || ((ExpandedFitness) bestOfGeneration[x].fitness).getFitnessScore()
                                > ((ExpandedFitness) bestSoFar[x].fitness).getFitnessScore()) {
                            bestSoFar[x] = (Individual) (bestOfGeneration[x].clone());
                        }
                        if(absoluteBest == null || ((ExpandedFitness) bestOfGeneration[x].fitness).getFitnessScore()
                                > ((ExpandedFitness) absoluteBest.fitness).getFitnessScore()) {
                            absoluteBest = (Individual) bestOfGeneration[x].clone();
                        }
                    }

                    // sum up mean fitness for population
                    totalFitnessThisGen[x] += ((ExpandedFitness) state.population.subpops[x].individuals[y].fitness).getFitnessScore();
                }
            }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

            // print out fitness information
            if (doSubpops) {
                state.output.print("" + meanFitnessThisGen[x] + " ", statisticslog);
                state.output.print("" + ((ExpandedFitness) bestOfGeneration[x].fitness).getFitnessScore() + " ", statisticslog);
                state.output.print("" + ((ExpandedFitness) bestSoFar[x].fitness).getFitnessScore() + " ", statisticslog);
            }
        }

        // Now gather per-Population statistics
        long popTotalInds = 0;
        double popMeanFitness = 0;
        double popTotalFitness = 0;
        Individual popBestOfGeneration = null;

        for (int x = 0; x < subpops; x++) {
            popTotalInds += totalIndsThisGen[x];
            popTotalFitness += totalFitnessThisGen[x];
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || ((ExpandedFitness) bestOfGeneration[x].fitness).getFitnessScore() > ((ExpandedFitness) popBestOfGeneration.fitness).getFitnessScore())) {
                popBestOfGeneration = bestOfGeneration[x];
            }
        }

        // build mean
        popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out

        // print out fitness info
        state.output.print("" + popMeanFitness + " ", statisticslog);                                                                                  // mean fitness of pop this gen
        state.output.print("" + (double) ((ExpandedFitness) popBestOfGeneration.fitness).getFitnessScore() + " ", statisticslog);                 // best fitness of pop this gen
        state.output.print("" + (double) ((ExpandedFitness) absoluteBest.fitness).getFitnessScore(), statisticslog);                // best fitness of pop so far

        // we're done!
        state.output.println("", statisticslog);
    }
}
