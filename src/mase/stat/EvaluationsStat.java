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
import mase.MaseProblem;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.stat.StatUtils.IndividualInfo;

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
        // header
        if(state.generation == 0) {
            EvaluationResult[] sample = ((ExpandedFitness) state.population.subpops[0].individuals[0].fitness).getEvaluationResults();
            state.output.println(header((MaseProblem) state.evaluator.p_problem, sample, mode.equals(V_BEST_SUB)), log);            
        }
        
        if (mode.equals(V_BEST)) {
            IndividualInfo best = StatUtils.getBest(state);
            state.output.println(entry(state.generation, best.sub, best.index, 
                    ((ExpandedFitness) best.ind.fitness).getEvaluationResults(), false), log);
        } else if (mode.equals(V_BEST_SUB)) {
            IndividualInfo[] subBests = StatUtils.getSubpopBests(state);
            for(IndividualInfo inf : subBests) {
            state.output.println(entry(state.generation, inf.sub, inf.index, 
                    ((ExpandedFitness) inf.ind.fitness).getEvaluationResults(), true), log);                
            }
        } else {
            // Generational log
            for (int i = 0; i < state.population.subpops.length; i++) {
                for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                    ExpandedFitness nf = (ExpandedFitness) state.population.subpops[i].individuals[j].fitness;
                    state.output.println(entry(state.generation, i, j, nf.getEvaluationResults(), false), log);
                }
            }
        }
        state.output.flush();
    }
    
    public static String header(MaseProblem prob, EvaluationResult[] sample, boolean allSubpops) {
        String header = "Generation Subpop Index";
        for (int i = 0; i < sample.length; i++) {
            String evalName = prob.getEvalFunctions()[i].getClass().getSimpleName();
            if (sample[i] instanceof SubpopEvaluationResult) {
                ArrayList<? extends EvaluationResult> allEvals = ((SubpopEvaluationResult) sample[i]).getAllEvaluations();
                if (allSubpops) {
                    for (int j = 0; j < allEvals.size(); j++) {
                        for (int k = 0; k < allEvals.get(j).toString().split(" ").length; k++) {
                            header += " " + evalName + "." + i + ".Sub." + j + "_" + k;
                        }
                    }
                } else {
                    for (int k = 0; k < allEvals.get(0).toString().split(" ").length; k++) {
                        header += " " + evalName + "." + i + "_" + k;
                    }
                }
            } else {
                for (int j = 0; j < sample[i].toString().split(" ").length; j++) {
                    header += " " + evalName + "." + i + "_" + j;
                }
            }
        }
        return header;
    }

    public static String entry(int gen, int sub, int index, EvaluationResult[] eval, boolean allSubpops) {
        String s = gen + " " + sub + " " + index;
        for (EvaluationResult er : eval) {
            if (er instanceof SubpopEvaluationResult) {
                if (allSubpops) {
                    ArrayList<? extends EvaluationResult> allEvals = ((SubpopEvaluationResult) er).getAllEvaluations();
                    for (EvaluationResult e : allEvals) {
                        s += " " + e;
                    }
                } else {
                    s += " " + ((SubpopEvaluationResult) er).getSubpopEvaluation(sub).toString();
                }
            } else {
                s += " " + er;
            }
        }
        return s;
    }
}
