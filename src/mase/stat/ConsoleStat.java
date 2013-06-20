/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import mase.ExpandedFitness;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ConsoleStat extends Statistics {

    static final double SMOOTH_CONSTANT = 0.5;
    long startTime;
    long lastTime;
    long genDuration;
    int jobs;
    int currentJob;
    DateFormat df;
    DecimalFormat nf;
    double bestFitness = Double.NEGATIVE_INFINITY;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        jobs = state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        currentJob = (Integer) state.job[0];
        df = new SimpleDateFormat("dd-HH:mm:ss");
        nf = new DecimalFormat("0.0000");
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void postBreedingStatistics(EvolutionState state) {
        super.postBreedingStatistics(state);
        long currentTime = System.currentTimeMillis();
        long instantDuration = lastTime == 0 ? currentTime - startTime : currentTime - lastTime;
        genDuration = genDuration == 0 ? instantDuration : Math.round(instantDuration * SMOOTH_CONSTANT + genDuration * (1 - SMOOTH_CONSTANT));
        long remainingJobTime = (state.numGenerations - state.generation - 1) * genDuration;
        long jobTime = (remainingJobTime + System.currentTimeMillis() - startTime);
        long remainingBatchTime = remainingJobTime + (jobs - currentJob - 1) * jobTime;

        Date jobEta = new Date(currentTime + remainingJobTime);
        Date batchEta = new Date(currentTime + remainingBatchTime);

        state.output.message("Gen: " + DurationFormatUtils.formatDuration(genDuration, "mm:ss:SSS")
                + " | Job: " + DurationFormatUtils.formatDuration(jobTime, "HH:mm:ss")
                + " | Job ETA: " + df.format(jobEta) + " (" + DurationFormatUtils.formatDuration(remainingJobTime, "HH:mm:ss") + ")"
                + " | Batch ETA: " + df.format(batchEta) + " (" + DurationFormatUtils.formatDuration(remainingBatchTime, "HH:mm:ss") + ")");

        lastTime = currentTime;
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        // print best fitness and min / 1st quartile / median / 3rd quartile / max per subpop
        double maxGen = Double.NEGATIVE_INFINITY;
        for(int s = 0 ; s < state.population.subpops.length ; s++)  {
            DescriptiveStatistics ds = new DescriptiveStatistics(state.population.subpops[s].individuals.length);
            for(Individual ind : state.population.subpops[s].individuals) {
                ds.addValue(((ExpandedFitness) ind.fitness).getFitnessScore());
            }
            state.output.message("Subpop " + s + ": " + nf.format(ds.getMin()) + " | " +
                    nf.format(ds.getPercentile(25)) + " | " + nf.format(ds.getMean()) + " | " +
                    nf.format(ds.getPercentile(75)) + " | " + nf.format(ds.getMax()));
            if(ds.getMax() > maxGen) {
                maxGen = ds.getMax();
            }
            if(ds.getMax() > bestFitness) {
                bestFitness = ds.getMax();
            }
        }
        state.output.message("Best gen: " + nf.format(maxGen) + " | " +
                "Best so far: " + nf.format(bestFitness));
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        state.output.message("Best fitness: " + nf.format(bestFitness));
    }
}
