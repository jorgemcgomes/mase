/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ScoresStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default

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
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        
        // header
        if(state.generation == 0) {
            ExpandedFitness sample = (ExpandedFitness) state.population.subpops[0].individuals[0].fitness;
            String header = "Generation Subpop Index";
            for(String s : sample.scores().keySet()) {
                header += " " + s;
            }
            header += " Score";
            state.output.println(header, log);
        }

        // generational log
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                state.output.print(state.generation + " " + i + " " + j, log);
                for(double score : nf.scores().values()) {
                    state.output.print(String.format(Locale.ENGLISH, " %.5f", score), log);
                }
                state.output.println(String.format(Locale.ENGLISH, " %.5f", nf.fitness()), log);
            }
        }
        state.output.getLog(log).writer.flush(); // flush        
    }
}
