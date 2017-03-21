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
import java.util.ArrayList;
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
    public static final String P_BEST_ONLY = "best-only";
    private static final long serialVersionUID = 1L;
    public int log;
    private boolean bestOnly = false;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(
                base.push(P_BEHAVIOURS_FILE), null);
        bestOnly = state.parameters.getBoolean(base.push(P_BEST_ONLY), null, false);
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
        if (bestOnly) {
            double bestFitness = Double.NEGATIVE_INFINITY;
            ExpandedFitness best = null;
            int sub = -1;
            int index = -1;
            for (int i = 0; i < state.population.subpops.length; i++) {
                for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                    ExpandedFitness f = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                    double fit = f.getFitnessScore();
                    if (fit > bestFitness) {
                        bestFitness = fit;
                        best = f;
                        sub = i;
                        index = j;
                    }
                }
            }

            // File header
            if (state.generation == 0) {
                String header = "Generation Subpop Index";
                EvaluationResult[] sample = best.getEvaluationResults();
                for (int i = 0; i < sample.length; i++) {
                    if (sample[i] instanceof SubpopEvaluationResult) {
                        ArrayList<EvaluationResult> allEvals = ((SubpopEvaluationResult) sample[i]).getAllEvaluations();
                        for (int j = 0 ; j < allEvals.size() ; j++) {
                            for (int k = 0; k < allEvals.get(j).toString().split(" ").length; k++) {
                                header += " Eval." + i + ".Sub." + j + "_" + k;
                            }                            
                        }
                    } else {
                        for(int j = 0 ; j < sample[i].toString().split(" ").length; j++) {
                            header += " Eval." + i + "_" + j;
                        }
                    }                    
                }
                state.output.println(header, log);
            }

            // Generational log
            state.output.print(state.generation + " " + sub + " " + index, log);
            for (EvaluationResult er : best.getEvaluationResults()) {
                if(er instanceof SubpopEvaluationResult) {
                    ArrayList<EvaluationResult> allEvals = ((SubpopEvaluationResult) er).getAllEvaluations();
                    for(EvaluationResult e : allEvals) {
                        state.output.print(" " + e, log);
                    }
                } else {
                    state.output.print(" " + er, log);
                }
            }
            state.output.println("", log);
        } else {
            // File header -- supports SubpopEvaluationResults with different evaluation lengths for each subpop
            if (state.generation == 0) {
                EvaluationResult[] sample = ((ExpandedFitness) state.population.subpops[0].individuals[0].fitness).getEvaluationResults();
                String header = "Generation Subpop Index";
                for (int i = 0; i < sample.length; i++) {
                    int max = 0;
                    if (sample[i] instanceof SubpopEvaluationResult) {
                        ArrayList<EvaluationResult> allEvals = ((SubpopEvaluationResult) sample[i]).getAllEvaluations();
                        for (EvaluationResult e : allEvals) {
                            max = Math.max(e.toString().split(" ").length, max);
                        }
                    } else {
                        max = sample[i].toString().split(" ").length;
                    }
                    for (int j = 0; j < max; j++) {
                        header += " Eval." + i + "_" + j;
                    }
                }
                state.output.println(header, log);
            }

             // Generational log
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
        }
        state.output.flush();
    }
}
