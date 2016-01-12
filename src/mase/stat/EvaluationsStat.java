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
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.ExpandedFitness;

/**
 * Generation -- sub-population number -- individual-index -- fitness --
 * behaviour characterisations -- newline
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class EvaluationsStat extends Statistics {

    public static final String P_BEHAVIOURS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(
                base.push(P_BEHAVIOURS_FILE), null);
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
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                state.output.print(state.generation + " " + i + " " + j, log);
                for (EvaluationResult er : nf.getEvaluationResults()) {
                    if (er instanceof SubpopEvaluationResult) {
                        SubpopEvaluationResult aer = (SubpopEvaluationResult) er;
                        state.output.print(" " + aer.getSubpopEvaluation(i).toString(), log);
                    } else {
                        state.output.print(" " + er.toString(), log);
                    }
                }
                state.output.print("\n", log);
            }
        }
        state.output.flush();
    }
}
