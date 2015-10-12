/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mase.evaluation.EvaluationResult;
import mase.evaluation.BehaviourResult;
import mase.evaluation.VectorBehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class SCResult extends VectorBehaviourResult {

    private static final long serialVersionUID = 1;
    protected Map<Integer, Double> counts;
    protected Map<Integer, byte[]> states;
    protected int removedByFilter;
    protected double[] rawClusteredCount;

    public SCResult(Map<Integer, Double> counts, Map<Integer, byte[]> states, Distance dist) {
        this.counts = counts;
        this.states = states;
        this.removedByFilter = 0;
        this.dist = dist;
        this.behaviour = null;
    }

    @Override
    public Object value() {
        if (behaviour != null) {
            return super.value();
        } else {
            return Pair.of(counts, states);
        }
    }

    @Override
    public SCResult mergeEvaluations(EvaluationResult[] results) {
        Map<Integer, Double> mergedCounts = new HashMap<>();
        Map<Integer, byte[]> mergedStates = new HashMap<>();
        for (EvaluationResult er : results) {
            SCPostEvaluator.mergeCountMap(mergedCounts, ((SCResult) er).getCounts());
            mergedStates.putAll(((SCResult) er).getStates());
        }
        return new SCResult(mergedCounts, mergedStates, dist);
    }

    @Override
    public double distanceTo(BehaviourResult br) {
        if (behaviour == null) {
            SCResult other = (SCResult) br;
            // make the counts vectors -- aligned by the same states
            Set<Integer> shared = new HashSet<>(this.counts.keySet());
            shared.retainAll(other.counts.keySet());
            int size = this.counts.size() + other.counts.size() - shared.size();
            double[] v1 = new double[size];
            double[] v2 = new double[size];
            int index = 0;
            // shared elements
            for (Integer h : shared) {
                v1[index] = this.counts.get(h);
                v2[index] = other.counts.get(h);
                index++;
            }
            // only elements from this
            for (Integer h : this.counts.keySet()) {
                if (!shared.contains(h)) {
                    v1[index] = this.counts.get(h);
                }
            }
            // only elements from other
            for (Integer h : other.counts.keySet()) {
                if (!shared.contains(h)) {
                    v2[index] = other.counts.get(h);
                }
            }
            return super.vectorDistance(v1, v2);
        } else {
            return super.distanceTo(br);
        }
    }



    @Override
    public String toString() {
        //return "SC";
        StringBuilder sb = new StringBuilder();
        for (Iterator<Entry<Integer, Double>> iter = counts.entrySet().iterator(); iter.hasNext();) {
            Entry<Integer, Double> e = iter.next();
            sb.append(e.getKey());
            sb.append(">");
            sb.append(e.getValue());
            if (iter.hasNext()) {
                sb.append(";");
            }
        }
        return new String(sb);
    }

    public Map<Integer, Double> getCounts() {
        return counts;
    }

    public Map<Integer, byte[]> getStates() {
        return states;
    }
}
