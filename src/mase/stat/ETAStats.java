/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ETAStats extends Statistics {

    static final double SMOOTH_CONSTANT = 0.5;
    long startTime;
    long lastTime;
    long genDuration;
    int jobs;
    int currentJob;
    DateFormat df;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        jobs = state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        currentJob = (Integer) state.job[0];
        df = new SimpleDateFormat("dd-HH:mm:ss");
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

        state.output.message("Gen duration: " + DurationFormatUtils.formatDuration(genDuration, "mm:ss:SSS")
                + " | Estimated job duration: " + DurationFormatUtils.formatDuration(jobTime, "HH:mm:ss")
                + " | Job ETA: " + df.format(jobEta) + " (" + DurationFormatUtils.formatDuration(remainingJobTime, "HH:mm:ss") + ")"
                + " | Batch ETA: " + df.format(batchEta) + " (" + DurationFormatUtils.formatDuration(remainingBatchTime, "HH:mm:ss") + ")");

        lastTime = currentTime;
    }
}
