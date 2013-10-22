/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mase.EvaluationResult;
import mase.evaluation.BehaviourResult;
import mase.evaluation.VectorBehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class SCResult extends VectorBehaviourResult {

    protected Map<Integer, Float> counts;
    protected Map<Integer, byte[]> states;
    protected int removedByFilter;

    public SCResult(Map<Integer, Float> counts, Map<Integer, byte[]> states, Distance dist) {
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
    public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
        Map<Integer, Float> mergedCounts = new HashMap<Integer, Float>();
        Map<Integer, byte[]> mergedStates = new HashMap<Integer, byte[]>();
        for (EvaluationResult er : results) {
            SCPostEvaluator.mergeCountMap(mergedCounts, ((SCResult) er).getCounts());
            mergedStates.putAll(((SCResult) er).getStates());
        }
        return new SCResult(mergedCounts, mergedStates, dist);
    }

    @Override
    public float distanceTo(BehaviourResult br) {
        if (behaviour == null) {
            SCResult other = (SCResult) br;
            // make the counts vectors -- aligned by the same states
            Set<Integer> shared = new HashSet<Integer>(this.counts.keySet());
            shared.retainAll(other.counts.keySet());
            int size = this.counts.size() + other.counts.size() - shared.size();
            float[] v1 = new float[size];
            float[] v2 = new float[size];
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



    /*@Override
    public String toString() {
        return "SC";
        StringBuilder sb = new StringBuilder();
        for (Iterator<Entry<Integer, Float>> iter = counts.entrySet().iterator(); iter.hasNext();) {
            Entry<Integer, Float> e = iter.next();
            sb.append(e.getKey());
            sb.append(">");
            sb.append(e.getValue());
            if (iter.hasNext()) {
                sb.append(";");
            }
        }
        return new String(sb);
    }*/

    public Map<Integer, Float> getCounts() {
        return counts;
    }

    public Map<Integer, byte[]> getStates() {
        return states;
    }
}
