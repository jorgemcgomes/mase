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
    public static final String P_MODE = "mode";
    public static final String V_ALL = "all", V_BEST = "best", V_BEST_SUB = "best-sub";
    private static final long serialVersionUID = 1L;
    public int log;
    private String mode;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(
                base.push(P_BEHAVIOURS_FILE), null);
        mode = state.parameters.getString(base.push(P_MODE), null);
        if (!(mode.equals(V_ALL) || mode.equals(V_BEST) || mode.equals(V_BEST_SUB))) {
            state.output.fatal("Unknown mode: " + mode, base.push(P_MODE));
        }
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
        if (mode.equals(V_BEST)) {
            ExpandedFitness best = null;
            double bestFitness = Double.NEGATIVE_INFINITY;
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
                EvaluationResult[] sample = best.getEvaluationResults();
                state.output.println(headerAll(sample), log);
            }

            // Generational log
            state.output.println(entryAll(state.generation, sub, index, best.getEvaluationResults()), log);
        } else if(mode.equals(V_BEST_SUB)) {
            // File header
            if(state.generation == 0) {
                EvaluationResult[] sample = ((ExpandedFitness) state.population.subpops[0].individuals[0].fitness).getEvaluationResults();
                state.output.println(headerSub(sample), log);                
            }
            
            // Generational log
            for (int i = 0; i < state.population.subpops.length; i++) {
                ExpandedFitness best = null;
                double bestFitness = Double.NEGATIVE_INFINITY;
                int sub = -1;
                int index = -1;                
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
                state.output.println(entrySub(state.generation, sub, index, best.getEvaluationResults()), log);
            }
        } else {
            // File header -- supports SubpopEvaluationResults with different evaluation lengths for each subpop
            if (state.generation == 0) {
                EvaluationResult[] sample = ((ExpandedFitness) state.population.subpops[0].individuals[0].fitness).getEvaluationResults();
                state.output.println(headerSub(sample), log);
            }

            // Generational log
            for (int i = 0; i < state.population.subpops.length; i++) {
                for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                    ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                    state.output.println(entrySub(state.generation, i, j, nf.getEvaluationResults()), log);
                }
            }
        }
        state.output.flush();
    }

    public static String headerAll(EvaluationResult[] sample) {
        String header = "Generation Subpop Index";
        for (int i = 0; i < sample.length; i++) {
            if (sample[i] instanceof SubpopEvaluationResult) {
                ArrayList<EvaluationResult> allEvals = ((SubpopEvaluationResult) sample[i]).getAllEvaluations();
                for (int j = 0; j < allEvals.size(); j++) {
                    for (int k = 0; k < allEvals.get(j).toString().split(" ").length; k++) {
                        header += " Eval." + i + ".Sub." + j + "_" + k;
                    }
                }
            } else {
                for (int j = 0; j < sample[i].toString().split(" ").length; j++) {
                    header += " Eval." + i + "_" + j;
                }
            }
        }
        return header;
    }

    public static String entryAll(int gen, int sub, int index, EvaluationResult[] eval) {
        String s = gen + " " + sub + " " + index;
        for (EvaluationResult er : eval) {
            if (er instanceof SubpopEvaluationResult) {
                ArrayList<EvaluationResult> allEvals = ((SubpopEvaluationResult) er).getAllEvaluations();
                for (EvaluationResult e : allEvals) {
                    s += " " + e;
                }
            } else {
                s += " " + er;
            }
        }
        return s;
    }

    public static String headerSub(EvaluationResult[] sample) {
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
        return header;
    }

    public static String entrySub(int gen, int sub, int index, EvaluationResult[] eval) {
        String s = gen + " " + sub + " " + index;
        for (EvaluationResult er : eval) {
            if (er instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult aer = (SubpopEvaluationResult) er;
                s += " " + aer.getSubpopEvaluation(sub).toString();
            } else {
                s += " " + er.toString();
            }
        }
        return s;
    }
}
