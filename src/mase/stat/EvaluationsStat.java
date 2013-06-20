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
import mase.EvaluationResult;
import mase.evaluation.AgentEvaluationResult;
import mase.ExpandedFitness;

/**
 * Generation -- sub-population number -- individual-index -- fitness --
 * behaviour characterisations -- newline
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class EvaluationsStat extends Statistics {

    public static final String P_BEHAVIOURS_FILE = "file";
    public static final String P_DO_BEHAVS = "do-behaviours";
    public int log;
    public boolean doBehaviours;

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
        doBehaviours = state.parameters.getBoolean(base.push(P_DO_BEHAVS), null, true);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        for (int i = 0; i < state.population.subpops.length; i++) {
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                state.output.print(state.generation + " " + i + " " + j, log);
                if (doBehaviours) {
                    for (EvaluationResult er : nf.getEvaluationResults()) {
                        if (er instanceof AgentEvaluationResult) {
                            AgentEvaluationResult aer = (AgentEvaluationResult) er;
                            state.output.print(" " + aer.getAgentEvaluation(i).toString(), log);
                        } else {
                            state.output.print(" " + er.toString(), log);
                        }
                    }
                } else {
                    state.output.print(" " + nf.getFitnessScore(), log);
                }
                state.output.print("\n", log);
            }
        }
    }
}
