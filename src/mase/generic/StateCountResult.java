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
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author jorge
 */
public class StateCountResult implements BehaviourResult {

    protected Map<Integer, Float> counts;
    protected Map<Integer, byte[]> states;
    protected double[] clusteredStateCount;
    protected int removedByFilter;

    public StateCountResult(Map<Integer, Float> counts, Map<Integer, byte[]> states) {
        this.counts = counts;
        this.states = states;
        this.removedByFilter = 0;
    }

    @Override
    public Object value() {
        if (clusteredStateCount == null) {
            throw new RuntimeException("The characterisation vector has not been set yet.");
        }
        return clusteredStateCount;
    }

    @Override
    public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
        Map<Integer, Float> mergedCounts = new HashMap<Integer, Float>();
        Map<Integer, byte[]> mergedStates = new HashMap<Integer, byte[]>();
        for (EvaluationResult er : results) {
            StateCountPostEvaluator.mergeCountMap(mergedCounts, ((StateCountResult) er).getCounts());
            mergedStates.putAll(((StateCountResult) er).getStates());
        }
        return new StateCountResult(mergedCounts, mergedStates);
    }

    @Override
    public float distanceTo(BehaviourResult br) {
        StateCountResult other = (StateCountResult) br;
        ArrayRealVector v1 = null;
        ArrayRealVector v2 = null;
        if (clusteredStateCount == null) {
            // make the vectors
            Set<Integer> shared = new HashSet<Integer>(this.counts.keySet());
            shared.retainAll(other.counts.keySet());
            int size = this.counts.size() + other.counts.size() - shared.size();
            v1 = new ArrayRealVector(size);
            v2 = new ArrayRealVector(size);
            int index = 0;
            // shared elements
            for(Integer h : shared) {
                v1.setEntry(index, this.counts.get(h));
                v2.setEntry(index, other.counts.get(h));
                index++;
            }
            // only elements from this
            for(Integer h : this.counts.keySet()) {
                if(!shared.contains(h)) {
                    v1.setEntry(index++, this.counts.get(h));
                }
            }
            // only elements from other
            for(Integer h : other.counts.keySet()) {
                if(!shared.contains(h)) {
                    v2.setEntry(index++, other.counts.get(h));
                }
            }
        } else {
            // vectors were already set, good to go
            v1 = new ArrayRealVector(this.clusteredStateCount);
            v2 = new ArrayRealVector(other.clusteredStateCount);
        }
        // cosine similarity
        return (float) (1 - v1.cosine(v2));
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

    public void setClusteredStateCount(double[] vector) {
        this.clusteredStateCount = vector;
    }

    public double[] getClusteredStateCount() {
        return clusteredStateCount;
    }

    public void clearStates() {
        this.states = null;
    }
}
