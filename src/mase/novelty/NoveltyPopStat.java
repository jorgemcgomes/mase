/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyPopStat extends Statistics {

    public static final String P_DO_SUBPOPS = "do-subpops";
    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default
    protected ArrayList<NoveltyEvaluation> neList = null;
    boolean doSubpops;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);

        if (statisticsFile != null) {
            try {
                log = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }

        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS), null, false);
        
        neList = new ArrayList<>();
        for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
            if (pe instanceof NoveltyEvaluation) {
                neList.add((NoveltyEvaluation) pe);
            }
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        // header
        state.output.println("Generation Subpop Archive MinNS MeanNS MaxNS", log);
    }
    
    

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);

        for (NoveltyEvaluation ne : neList) {
            DescriptiveStatistics ds = new DescriptiveStatistics();        
            for (int i = 0; i < state.population.subpops.size(); i++) {
                for (Individual individual : state.population.subpops.get(i).individuals) {
                    ExpandedFitness nf = (ExpandedFitness) individual.fitness;
                    ds.addValue(nf.scores().get(ne.scoreName));
                }
                if(doSubpops) {
                    state.output.println(state.generation + " " + i + " " + ne.archives[i].size() + " " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax() , log);
                    ds.clear();
                }
            }
            if(!doSubpops) {
                state.output.println(state.generation + " NA " + ne.archives[0].size() + " " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax() , log);
            }
        }
    }
}
