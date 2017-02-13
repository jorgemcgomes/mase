/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mase.evaluation.EvaluationResult;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author jorge
 */
public class SCResult implements BehaviourResult {

    private static final long serialVersionUID = 1;
    protected Map<Integer, Integer> counts;
    protected int originalSize;

    public SCResult(Map<Integer, Integer> counts) {
        this.counts = counts;
        this.originalSize = counts.size();
    }

    @Override
    public Object value() {
        return counts;
    }

    @Override
    public SCResult mergeEvaluations(EvaluationResult[] results) {
        SCResult first = (SCResult) results[0];
        int totalOriginalSize = first.originalSize;
        Map<Integer, Integer> mergedCounts = (Map<Integer, Integer>) ((HashMap) first.counts).clone();
        for (int i = 1 ; i < results.length ; i++) {
            SCResult r = (SCResult) results[i];
            totalOriginalSize += r.originalSize;
            mergeCountMap(mergedCounts, r.counts);
        }
        SCResult newRes = new SCResult(mergedCounts);
        newRes.originalSize = totalOriginalSize;
        return newRes;
    }

    @Override
    public double distanceTo(BehaviourResult br) {
        SCResult other = (SCResult) br;
        int diffs = 0;
        int total = 0;
        
        // set intersection -- shared states
        Set<Integer> shared = new HashSet<>(this.counts.keySet());
        shared.retainAll(other.counts.keySet());
               
        // shared elements
        for (Integer h : shared) {
            int c1 = this.counts.get(h);
            int c2 = other.counts.get(h);
            diffs += Math.abs(c1 - c2);
            total += c1 + c2;
        }
        // The GECCO paper probably had a bug in the next code, as it was not
        // incrementing the index. This means that only the shared elements
        // where being effectively used for the comparison

        // only elements from this
        for (Integer h : this.counts.keySet()) {
            if (!shared.contains(h)) {
                int c = this.counts.get(h);
                diffs += c;
                total += c;
            }
        }
        // only elements from other
        for (Integer h : other.counts.keySet()) {
            if (!shared.contains(h)) {
                int c = other.counts.get(h);
                diffs += c;
                total += c;
            }
        }
        return (double) diffs / total;
    }


    @Override
    public String toString() {
        return counts.size() + " " + (originalSize - counts.size());
        /*
        // the states and counts pairs, sorted by counts
        List<Entry<Integer,Integer>> list = new ArrayList<>(counts.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer,Integer>>() {
            @Override
            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });
        StringBuilder sb = new StringBuilder();
        for (Entry<Integer, Integer> e : list) {
            sb.append(e.getKey()).append(">").append(e.getValue()).append(";");
        }
        return new String(sb);*/
    }

    public Map<Integer, Integer> getCounts() {
        return counts;
    }

    protected void filter(double percentageThreshold) {
        int totalCount = 0;
        for (Integer c : getCounts().values()) {
            totalCount += c;
        }
        int threshold = (int) Math.round(totalCount * percentageThreshold);
        filter(threshold);
    }
    
    protected void filter(int countThreshold) {
        // fail-safe in case all are to be removed
        // retain only the element with highest count
        Entry<Integer,Integer> highestCount = null;
        for(Entry<Integer,Integer> e : counts.entrySet()) {
            if(highestCount == null || e.getValue() > highestCount.getValue()) {
                highestCount = e;
            }
        }
        
        for(Iterator<Map.Entry<Integer, Integer>> it = counts.entrySet().iterator(); it.hasNext(); ) {
            Entry<Integer, Integer> next = it.next();
            if(next.getValue() < countThreshold) {
                it.remove();
            }
        }
        
        if(counts.isEmpty()) {
            counts.put(highestCount.getKey(), highestCount.getValue());
        }     
    }

    
    protected static void mergeCountMap(Map<Integer, Integer> map, Map<Integer, Integer> other) {
        for (Map.Entry<Integer, Integer> e : other.entrySet()) {
            if (!map.containsKey(e.getKey())) {
                map.put(e.getKey(), e.getValue());
            } else {
                map.put(e.getKey(), map.get(e.getKey()) + e.getValue());
            }
        }
    }
}
