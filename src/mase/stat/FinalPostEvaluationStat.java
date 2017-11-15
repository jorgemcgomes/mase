/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.eval.MasterProblem;
import ec.util.Parameter;
import mase.MaseProblem;

/**
 *
 * @author jorge
 */
public class FinalPostEvaluationStat extends Statistics {

    public static final String P_REPETITIONS = "repetitions";
    public static final String P_ALL_SUBPOPS = "all-subpops";
    public static final String P_PREFIX = "prefix";
    private static final long serialVersionUID = 1L;

    private int repetitions;
    private String prefix;
    private boolean allSubs;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.repetitions = state.parameters.getInt(base.push(P_REPETITIONS), null);
        this.prefix = state.parameters.getString(base.push(P_PREFIX), null);
        this.allSubs = state.parameters.getBoolean(base.push(P_ALL_SUBPOPS), null, false);
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        MaseProblem prob = (MaseProblem) (state.evaluator.p_problem instanceof MasterProblem ? 
                ((MasterProblem) state.evaluator.p_problem).problem.clone() : 
                state.evaluator.p_problem.clone());
        BatchReevaluate reav = new BatchReevaluate(repetitions, allSubs, true, prefix);

        BestSolutionGenStat best = null;
        for (Statistics stat : state.statistics.children) {
            if (stat instanceof BestSolutionGenStat) {
                best = (BestSolutionGenStat) stat;
                break;
            }
        }
        if (best != null) {
            try {
                reav.reevaluateTar(best.archiveFile, prob);
            } catch (Exception ex) {
                state.output.warning("Post-evaluation error: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            state.output.warning("Cannot find BestSolutionGenStat. Not performing post-evaluation");
        }
    }
}
