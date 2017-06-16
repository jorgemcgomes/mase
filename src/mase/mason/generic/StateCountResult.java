/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import java.util.Collection;
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
public class StateCountResult implements BehaviourResult<Map<Integer, Integer>> {

    private static final long serialVersionUID = 1;
    protected Map<Integer, Integer> counts;
    protected int originalSize; // for statistics only

    public StateCountResult(Map<Integer, Integer> counts) {
        this.counts = counts;
        this.originalSize = counts.size(); // for statistics only
    }

    @Override
    public Map<Integer, Integer> value() {
        return counts;
    }

    @Override
    public StateCountResult mergeEvaluations(Collection<EvaluationResult<Map<Integer, Integer>>> results) {
        int totalOriginalSize = 0;
        Map<Integer, Integer> mergedCounts = new HashMap<>();
        for (EvaluationResult<Map<Integer, Integer>> e : results) {
            totalOriginalSize += ((StateCountResult) e).originalSize; // for statistics only
            mergeCountMap(mergedCounts, e.value());
        }
        StateCountResult newRes = new StateCountResult(mergedCounts);
        newRes.originalSize = totalOriginalSize; // for statistics only
        return newRes;

    }

    @Override
    public double distanceTo(BehaviourResult<Map<Integer, Integer>> other) {
        int diffs = 0;
        int total = 0;

        // set intersection -- shared states
        Set<Integer> shared = new HashSet<>(this.counts.keySet());
        shared.retainAll(other.value().keySet());

        // shared elements
        for (Integer h : shared) {
            int c1 = this.counts.get(h);
            int c2 = other.value().get(h);
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
        for (Integer h : other.value().keySet()) {
            if (!shared.contains(h)) {
                int c = other.value().get(h);
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

    protected void filter(double percentageThreshold) {
        int totalCount = 0;
        for (Integer c : value().values()) {
            totalCount += c;
        }
        int threshold = (int) Math.round(totalCount * percentageThreshold);
        filter(threshold);
    }

    protected void filter(int countThreshold) {
        // fail-safe in case all are to be removed
        // retain only the element with highest count
        Entry<Integer, Integer> highestCount = null;
        for (Entry<Integer, Integer> e : counts.entrySet()) {
            if (highestCount == null || e.getValue() > highestCount.getValue()) {
                highestCount = e;
            }
        }

        for (Iterator<Map.Entry<Integer, Integer>> it = counts.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Integer> next = it.next();
            if (next.getValue() < countThreshold) {
                it.remove();
            }
        }

        if (counts.isEmpty()) {
            counts.put(highestCount.getKey(), highestCount.getValue());
        }
    }

    protected static void mergeCountMap(Map<Integer, Integer> map, Map<Integer, Integer> other) {
        if (!other.isEmpty()) {
            if (map.isEmpty()) {
                map.putAll(other);
            } else {
                for (Map.Entry<Integer, Integer> e : other.entrySet()) {
                    if (!map.containsKey(e.getKey())) {
                        map.put(e.getKey(), e.getValue());
                    } else {
                        map.put(e.getKey(), map.get(e.getKey()) + e.getValue());
                    }
                }
            }
        }
    }

}
