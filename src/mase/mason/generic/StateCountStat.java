/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.CompoundEvaluationResult;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class StateCountStat extends Statistics {

    public static final String P_LOG_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int genLog;
    protected Map<Integer, Integer> globalCount;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        System.out.println("");
        super.setup(state, base);
        try {
            File logFile = state.parameters.getFile(base.push(P_LOG_FILE), null);
            genLog = state.output.addLog(logFile, true, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the state count logs.");
        }
        this.globalCount = new HashMap<>(100000);
    }

    /**
     * Generation | Total unique states | Unique states this gen | total count
     * this gen | mean filtered (absolute) | min/mean/max filtered (relative) |
     * min/mean/max unique states in eval
     *
     * @param state
     */
    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        Map<Integer, Integer> genCount = new HashMap<>(1000);
        DescriptiveStatistics filteredAbs = new DescriptiveStatistics();
        DescriptiveStatistics filteredRel = new DescriptiveStatistics();
        DescriptiveStatistics sizeStat = new DescriptiveStatistics();

        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof CompoundEvaluationResult && ((CompoundEvaluationResult) er).value().get(0) instanceof StateCountResult) {
                        CompoundEvaluationResult<StateCountResult> ser = (CompoundEvaluationResult<StateCountResult>) er;
                        List<StateCountResult> all = ser.value();
                        for (StateCountResult scr : all) {
                            StateCountResult.mergeCountMap(genCount, scr.value());
                            filteredAbs.addValue(scr.originalSize - scr.value().size());
                            filteredRel.addValue(1 - (double) scr.value().size() / scr.originalSize);
                            sizeStat.addValue(scr.value().size());
                        }
                    }
                }
            }
        }
        int totalCount = 0;
        for (Integer i : genCount.values()) {
            totalCount += i;
        }
        StateCountResult.mergeCountMap(globalCount, genCount);
        state.output.println(state.generation + " " + globalCount.size() + " "
                + genCount.size() + " " + totalCount + " " + filteredAbs.getMean() + " " + filteredRel.getMin() + " "
                + filteredRel.getMean() + " " + filteredRel.getMax() + " " + sizeStat.getMin() + " "
                + sizeStat.getMean() + " " + sizeStat.getMax(), genLog);
    }

}
