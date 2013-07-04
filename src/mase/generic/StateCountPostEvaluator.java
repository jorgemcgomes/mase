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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import mase.EvaluationResult;
import mase.ExpandedFitness;
import mase.PostEvaluator;
import mase.evaluation.AgentEvaluationResult;

/**
 *
 * @author jorge
 */
public class StateCountPostEvaluator implements PostEvaluator {

    public static final String P_STATECOUNT_BASE = "statecount";
    public static final String P_NUM_CLUSTERS = "k-clusters";
    public static final String P_FILTER = "filter-threshold";
    public static final String P_DO_FILTER = "do-filter";
    public static final String P_DO_CLUSTER = "do-cluster";
    public static final String P_DO_TFIDF = "do-tf-idf";
    protected int numClusters;
    protected double filter;
    protected boolean doFilter;
    protected boolean doCluster;
    protected Map<Integer, byte[]> pending;
    protected Map<Integer, byte[]> globalKey;
    protected Map<Integer, Float> globalCount; // TODO: se isto nao for usado aqui, passar para o Stat
    protected ArrayList<float[]> clusters;
    protected Map<Integer, float[]> clusterAssignements;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        //this.numClusters = state.parameters.getInt(base.push(P_NUM_CLUSTERS), null);
        Parameter df = new Parameter(P_STATECOUNT_BASE);
        this.filter = state.parameters.getDouble(base.push(P_FILTER), df.push(P_FILTER));
        this.doCluster = state.parameters.getBoolean(base.push(P_DO_CLUSTER), df.push(P_DO_CLUSTER), false);
        this.doFilter = state.parameters.getBoolean(base.push(P_DO_FILTER), df.push(P_DO_FILTER), false);
        this.globalKey = new HashMap<Integer, byte[]>(1000);
        this.globalCount = new HashMap<Integer, Float>(1000);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        if (doCluster) {
            rebuildClusters();
        }
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof StateCountResult) {
                        processResult((StateCountResult) er);
                    } else if (er instanceof AgentEvaluationResult) {
                        EvaluationResult[] ers = ((AgentEvaluationResult) er).getAllEvaluations();
                        for (EvaluationResult suber : ers) {
                            if(suber instanceof StateCountResult) {
                                processResult((StateCountResult) suber);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void rebuildClusters() {
        // integrate the states in pending, and clear pending
        // rebuild the cluster assignements (potential bottleneck!)
        // build kdtree with clusters to allow log(n) nearest search?
        // call cluster for every element in the archive (in case novelty search is being used!)
    }

    protected void processResult(StateCountResult res) {
        if (doFilter) {
            filter(res);
        }
        integrate(res);
        if (doCluster) {
            cluster(res);
        }
    }

    protected void filter(StateCountResult res) {
        // remove the entries with very low frequency count
        int numStates = res.getCounts().size();
        double total = 0;
        for (Float f : res.getCounts().values()) {
            total += f;
        }
        double threshold = total * filter;
        for (Iterator<Entry<Integer, Float>> it = res.getCounts().entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Float> next = it.next();
            if (next.getValue() < threshold) {
                it.remove();
                res.getStates().remove(next.getKey());
            }
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

    protected void integrate(StateCountResult res) {
        // if it contains new states, add them to globalKey, make the cluster assignement, and add them to pending
        for (Entry<Integer, byte[]> e : res.getStates().entrySet()) {
            if (!globalKey.containsKey(e.getKey())) {
                globalKey.put(e.getKey(), e.getValue());
            }
        }
        res.clearStates(); // res key is no longer necessary
        mergeCountMap(globalCount, res.getCounts());
    }

    protected void cluster(StateCountResult res) {
        // cluster and aggregate counts
        // make vector and store it in res
    }
}
