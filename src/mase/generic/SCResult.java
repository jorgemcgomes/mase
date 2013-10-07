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
import mase.EvaluationResult;
import mase.evaluation.BehaviourResult;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author jorge
 */
public class SCResult implements BehaviourResult {

    public enum Distance {

        COSINE, BRAY_CURTIS
    };
    protected Map<Integer, Float> counts;
    protected Map<Integer, byte[]> states;
    protected int removedByFilter;
    protected Distance dist;
    private ArrayRealVector clustered;

    public SCResult(Map<Integer, Float> counts, Map<Integer, byte[]> states, Distance dist) {
        this.counts = counts;
        this.states = states;
        this.removedByFilter = 0;
        this.dist = dist;
    }

    @Override
    public Object value() {
        if (clustered != null) {
            return clustered;
        } else {
            return Pair.of(counts, states);
        }
    }

    protected void setClustered(ArrayRealVector cl) {
        this.clustered = cl;
    }

    protected ArrayRealVector getClustered() {
        return clustered;
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
        ArrayRealVector v1, v2;
        if (clustered == null) {
            SCResult other = (SCResult) br;
            // make the counts vectors -- aligned by the same states
            Set<Integer> shared = new HashSet<Integer>(this.counts.keySet());
            shared.retainAll(other.counts.keySet());
            int size = this.counts.size() + other.counts.size() - shared.size();
            v1 = new ArrayRealVector(size);
            v2 = new ArrayRealVector(size);
            int index = 0;
            // shared elements
            for (Integer h : shared) {
                v1.setEntry(index, this.counts.get(h));
                v2.setEntry(index, other.counts.get(h));
                index++;
            }
            // only elements from this
            for (Integer h : this.counts.keySet()) {
                if (!shared.contains(h)) {
                    v1.setEntry(index++, this.counts.get(h));
                }
            }
            // only elements from other
            for (Integer h : other.counts.keySet()) {
                if (!shared.contains(h)) {
                    v2.setEntry(index++, other.counts.get(h));
                }
            }
        } else {
            v1 = this.clustered;
            v2 = ((SCResult) br).clustered;
        }
        return vectorDistance(v1, v2);
    }

    protected float vectorDistance(ArrayRealVector v1, ArrayRealVector v2) {
        switch (dist) {
            case BRAY_CURTIS:
                float diffs = 0;
                float total = 0;
                for (int i = 0; i < v1.getDimension(); i++) {
                    diffs += Math.abs(v1.getEntry(i) - v2.getEntry(i));
                    total += v1.getEntry(i) + v2.getEntry(i);
                }
                return diffs / total;
            default:
            case COSINE:
                return (float) (1 - v1.cosine(v2));
        }
    }

    @Override
    public String toString() {
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
    }

    public Map<Integer, Float> getCounts() {
        return counts;
    }

    public Map<Integer, byte[]> getStates() {
        return states;
    }
}
