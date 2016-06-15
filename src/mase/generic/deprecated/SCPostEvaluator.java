/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.PostEvaluator;

/**
 *
 * @author jorge
 */
public class SCPostEvaluator implements PostEvaluator {

    public static final String P_STATECOUNT_BASE = "statecount";
    public static final String P_FILTER = "filter-threshold";
    public static final String P_DO_FILTER = "do-filter";
    public static final String P_DO_TFIDF = "do-tf-idf";
    private static final long serialVersionUID = 1L;
    protected double filter;
    protected boolean doFilter;
    protected boolean doTfIdf;
    protected List<SCResult> currentPop;
    protected Map<Integer, byte[]> globalKey;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        Parameter df = new Parameter(P_STATECOUNT_BASE);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), df.push(P_FILTER));
        this.doFilter = state.parameters.getBoolean(base.push(P_DO_FILTER), df.push(P_DO_FILTER), false);
        this.doTfIdf = state.parameters.getBoolean(base.push(P_DO_TFIDF), df.push(P_DO_TFIDF), false);
        this.globalKey = new HashMap<>(10000);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        // Auxiliary data structure
        currentPop = new ArrayList<>(state.population.subpops.length
                * state.population.subpops[0].individuals.length);
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof SCResult) {
                        SCResult scr = (SCResult) er;
                        currentPop.add(scr);
                        globalKey.putAll(scr.getStates());
                        scr.states = null;
                    }
                }
            }
        }

        // Filter
        if (doFilter) {
            for (SCResult scr : currentPop) {
                scr.filter(filter);
            }
        }
    }

    public Map<Integer, byte[]> getGlobalKey() {
        return globalKey;
    }
}
