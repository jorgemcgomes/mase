/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author jorge
 */
public class StateCountResultBrayCurtis extends StateCountResult {

    public StateCountResultBrayCurtis(Map<Integer, Float> counts, Map<Integer, byte[]> states) {
        super(counts, states);
    }

    @Override
    public float distanceTo(BehaviourResult br) {
        StateCountResultBrayCurtis other = (StateCountResultBrayCurtis) br;
        double distance = 0;
        if (clusteredStateCount == null) {
            Set<Integer> shared = new HashSet<Integer>(this.counts.keySet());
            shared.retainAll(other.counts.keySet());
            // shared elements
            for (Integer h : shared) {
                distance += Math.abs(this.counts.get(h) - other.counts.get(h));
            }
            // only elements from this
            for (Integer h : this.counts.keySet()) {
                if (!shared.contains(h)) {
                    distance += this.counts.get(h);
                }
            }
            // only elements from other
            for (Integer h : other.counts.keySet()) {
                if (!shared.contains(h)) {
                    distance += other.counts.get(h);
                }
            }
            double totalThis = 0;
            for(Float f : this.counts.values()) {
                totalThis += f;
            }
            double totalOther = 0;
            for(Float f : other.counts.values()) {
                totalOther += f;
            }
            distance = distance / (totalThis + totalOther);
        }
        return (float) distance;
    }
}
