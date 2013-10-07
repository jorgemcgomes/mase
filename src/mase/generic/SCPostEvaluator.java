/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mase.EvaluationResult;
import mase.ExpandedFitness;
import mase.PostEvaluator;

/**
 *
 * @author jorge
 */
public class SCPostEvaluator implements PostEvaluator {

    public static final String P_STATECOUNT_BASE = "statecount";
    public static final String P_FILTER = "filter-threshold";
    public static final String P_DO_FILTER = "do-filter";
    public static final String P_DO_TFIDF = "do-tf-idf";
    protected double filter;
    protected boolean doFilter;
    protected boolean doTfIdf;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        Parameter df = new Parameter(P_STATECOUNT_BASE);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), df.push(P_FILTER));
        this.doFilter = state.parameters.getBoolean(base.push(P_DO_FILTER), df.push(P_DO_FILTER), false);
        this.doTfIdf = state.parameters.getBoolean(base.push(P_DO_TFIDF), df.push(P_DO_TFIDF), false);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof SCResult) {
                        if (doFilter) {
                            filter((SCResult) er);
                        }
                    }
                }
            }
        }
    }

    protected void filter(SCResult res) {
        // remove the entries with very low frequency count
        int numStates = res.getCounts().size();
        double total = 0;
        for (Float f : res.getCounts().values()) {
            total += f;
        }
        double threshold = total * filter;

        List<Integer> toRemove = new LinkedList<Integer>();
        Entry<Integer, Float> highestCount = null;
        for (Entry<Integer, Float> e : res.getCounts().entrySet()) {
            if (e.getValue() < threshold) {
                toRemove.add(e.getKey());
            }
            if (highestCount == null || e.getValue() > highestCount.getValue()) {
                highestCount = e;
            }
        }
        if (toRemove.size() < res.getCounts().size()) {
            res.getCounts().keySet().removeAll(toRemove);
            res.getStates().keySet().removeAll(toRemove);
        } else { // retain only the element with highest count
            res.getCounts().clear();
            res.getCounts().put(highestCount.getKey(), highestCount.getValue());
            byte[] state = res.getStates().get(highestCount.getKey());
            res.getStates().clear();
            res.getStates().put(highestCount.getKey(), state);
        }

        res.removedByFilter = numStates - res.getCounts().size();
    }

    protected static void mergeCountMap(Map<Integer, Float> map, Map<Integer, Float> other) {
        for (Map.Entry<Integer, Float> e : other.entrySet()) {
            if (!map.containsKey(e.getKey())) {
                map.put(e.getKey(), e.getValue());
            } else {
                map.put(e.getKey(), map.get(e.getKey()) + e.getValue());
            }
        }
    }
}
