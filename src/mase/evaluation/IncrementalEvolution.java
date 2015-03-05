/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class IncrementalEvolution extends Statistics {

    public static final String DEFAULT_BASE = "incremental";
    public static final String P_NUM_STAGES = "num-stages";
    public static final String P_FITNESS_THRESHOLD = "fitness-threshold";
    public static final String P_ABOVE_THRESHOLD = "above-threshold";
    public static final String P_FILE = "file";
    public static final String P_MIN_GENERATIONS = "min-generations";
    
    protected int currentStage;
    protected int currentStageStart;
    protected int numStages;
    protected double[] fitnessThreshold;
    protected double[] aboveThreshold;
    protected int minGenerations;
    protected int statisticslog;

    public Parameter defaultBase() {
        return new Parameter(DEFAULT_BASE);
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = new Parameter(DEFAULT_BASE);
        this.currentStage = 0;
        this.currentStageStart = 0;
        this.numStages = state.parameters.getInt(base.push(P_NUM_STAGES), df.push(P_NUM_STAGES));
        this.minGenerations = state.parameters.getInt(base.push(P_MIN_GENERATIONS), df.push(P_MIN_GENERATIONS));

        this.fitnessThreshold = new double[numStages];
        String s = state.parameters.getString(base.push(P_FITNESS_THRESHOLD), df.push(P_FITNESS_THRESHOLD));
        String[] split = s.split(",");
        if(split.length == 1) {
            Arrays.fill(fitnessThreshold, Double.parseDouble(split[0]));
        } else if(split.length == fitnessThreshold.length) {
            for(int i = 0 ; i < split.length ; i++) {
                fitnessThreshold[i] = Double.parseDouble(split[i]);
            }
        } else {
            state.output.fatal("Fitness threshold: mismatch between number of stages (" + numStages + ") and fitness thresholds (" + split.length + ")");
        }
        
        this.aboveThreshold = new double[numStages];
        s = state.parameters.getString(base.push(P_ABOVE_THRESHOLD), df.push(P_ABOVE_THRESHOLD));
        split = s.split(",");
        if(split.length == 1) {
            Arrays.fill(aboveThreshold, Double.parseDouble(split[0]));
        } else if(split.length == aboveThreshold.length) {
            for(int i = 0 ; i < split.length ; i++) {
                aboveThreshold[i] = Double.parseDouble(split[i]);
            }
        } else {
            state.output.fatal("Above threshold: mismatch between number of stages (" + numStages + ") and above thresholds (" + split.length + ")");
        }
        
        File statisticsFile = state.parameters.getFile(base.push(P_FILE), df.push(P_FILE));
        if (statisticsFile != null) {
            try {
                statisticslog = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        changeStage(state, currentStage);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.preEvaluationStatistics(state);

        int above = 0;
        int all = 0;
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                if (ef.getFitnessScore() >= fitnessThreshold[currentStage]) {
                    above++;
                }
                all++;
            }
        }

        double aboveFraction = (double) above / all;

        state.output.println(state.generation + " " + aboveFraction + " " + aboveThreshold[currentStage] + " " + fitnessThreshold[currentStage] + " " + currentStage, statisticslog);

        if (state.generation - currentStageStart >= minGenerations && aboveFraction >= aboveThreshold[currentStage] && currentStage < numStages - 1) {
            currentStage++;
            currentStageStart = state.generation;
            changeStage(state, currentStage);
        }

    }

    public void changeStage(EvolutionState state, int stage) {
        state.output.message("*************** CHANGING TO STAGE " + stage + " **************");
    }

}
