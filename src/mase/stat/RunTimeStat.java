/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import mase.evaluation.MetaEvaluator;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Should be the last Statistic in the list in order to obtain accurate results
 * @author jorge
 */
public class RunTimeStat extends Statistics {
    
    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int log;
    
    private long runStartTime = 0;
    private long totalGenTime = 0;
    private long breedingTime = 0;
    private long totalEvalTime = 0;
    private long evalTime = 0;
    private long postEvalTime = 0;
    private long preBreedingExchTime = 0;
    private long postBreedingExchTime = 0;
    
    
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
        runStartTime = System.currentTimeMillis();
        totalGenTime = runStartTime;
        
        state.output.println("Generation Duration TimeStamp TotalGen TotalEval Eval PostEvals PreBreed Breed PostBreed", log);
    }
        
    @Override
    public void preEvaluationStatistics(EvolutionState state) {
        super.preEvaluationStatistics(state); 
        totalEvalTime = totalGenTime;
    }
    
    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        totalEvalTime = System.currentTimeMillis() - totalEvalTime;
        if(state.evaluator instanceof MetaEvaluator) {
            MetaEvaluator me = (MetaEvaluator) state.evaluator;
            evalTime = me.lastEvaluationTime;
            postEvalTime = me.lastPostEvaluationTime;
        }
    }
    
    @Override
    public void prePreBreedingExchangeStatistics(EvolutionState state) {
        super.prePreBreedingExchangeStatistics(state);
        preBreedingExchTime = System.currentTimeMillis();
    }
    
    @Override
    public void postPreBreedingExchangeStatistics(EvolutionState state) {
        super.postPreBreedingExchangeStatistics(state);
        preBreedingExchTime = System.currentTimeMillis() - preBreedingExchTime;
    }    
    
    @Override
    public void preBreedingStatistics(EvolutionState state) {
        super.preBreedingStatistics(state);
        breedingTime = System.currentTimeMillis();
    }

    @Override
    public void postBreedingStatistics(EvolutionState state) {
        super.postBreedingStatistics(state);
        breedingTime = System.currentTimeMillis() - breedingTime;
    }
    
    @Override
    public void prePostBreedingExchangeStatistics(EvolutionState state) {
        super.prePostBreedingExchangeStatistics(state);
        postBreedingExchTime = System.currentTimeMillis();
    }
    
    @Override
    public void postPostBreedingExchangeStatistics(EvolutionState state) {
        super.postPostBreedingExchangeStatistics(state); 
        postBreedingExchTime = System.currentTimeMillis() - postBreedingExchTime;
        
        // WRITE STAT AND ZERO TIME STATS
        totalGenTime = System.currentTimeMillis() - totalGenTime;
        writeStats(state);
        consoleStats(state);
        
        totalGenTime = System.currentTimeMillis();
        totalEvalTime = 0;
        evalTime = 0;
        postEvalTime = 0;
        preBreedingExchTime = 0;
        breedingTime = 0;
        postBreedingExchTime = 0;
    }
    
    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        
        // WRITE STATS
        totalGenTime = System.currentTimeMillis() - totalGenTime;
        writeStats(state);
    }

    private void writeStats(EvolutionState state) {
        long timeSoFar = System.currentTimeMillis() - runStartTime;
        Date cur = new Date();
        
        state.output.println(state.generation + " " + DurationFormatUtils.formatDurationHMS(timeSoFar) +  " " +
                DateFormatUtils.format(cur, "dd.MM.yy-HH:mm:ss") + " " + totalGenTime + " " + 
                totalEvalTime + " " + evalTime + " " + postEvalTime + " " + preBreedingExchTime + " " +
                breedingTime + " " + postBreedingExchTime, log);
    }
    
    private void consoleStats(EvolutionState state) {
        long eta = eta(state);
        Date etaDate = new Date(System.currentTimeMillis() + eta);
        state.output.message("Gen: " + DurationFormatUtils.formatDuration(totalGenTime, "mm:ss.SSS")
                + " | Run: " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - runStartTime, "HH:mm:ss")
                + " | ETA: " + DateFormatUtils.format(etaDate, "HH:mm:ss")
                + " [" + DurationFormatUtils.formatDuration(eta, "HH:mm:ss") + "]\n");
    }
    
    public long eta(EvolutionState state) {
        MetaEvaluator me = (MetaEvaluator) state.evaluator;
        double completed = me.maxEvaluations > 0 ?
                (double) me.totalEvaluations / me.maxEvaluations :
                (double) (state.generation + 1) / state.numGenerations;
        if(completed == 0) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - runStartTime;
        long remaining = Math.round(((1 - completed) * elapsed) / completed);
        return remaining;        
    }
}
    /*@Override
    public void postBreedingStatistics(EvolutionState state) {
        super.postBreedingStatistics(state);
        long jobTime = (remainingJobTime + System.currentTimeMillis() - startTime);
        long remainingBatchTime = remainingJobTime + (jobs - currentJob - 1) * jobTime;

        Date jobEta = new Date(currentTime + remainingJobTime);
        Date batchEta = new Date(currentTime + remainingBatchTime);

        state.output.message("Gen: " + DurationFormatUtils.formatDuration(genDuration, "mm:ss:SSS")
                + " | Job: " + DurationFormatUtils.formatDuration(jobTime, "HH:mm:ss")
                + " | Job ETA: " + df.format(jobEta) + " (" + DurationFormatUtils.formatDuration(remainingJobTime, "HH:mm:ss") + ")"
                + " | Batch ETA: " + df.format(batchEta) + " (" + DurationFormatUtils.formatDuration(remainingBatchTime, "HH:mm:ss") + ")");
        
        lastTime = currentTime;
    }*/
