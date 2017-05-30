/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.VectorBehaviourResult;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author jorge
 */
public class MESubpopulation extends Subpopulation {

    public static final String P_INITIAL_BATCH = "initial-batch";
    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    public static final String P_RESOLUTION = "resolution";
    private static final long serialVersionUID = 1L;
    protected int batchSize;
    protected int behaviourIndex;
    protected double resolution;
    protected int newInRepo = 0;

    // each unique key is a bin, can have multiple individuals in it
    protected MultiValuedMap<Integer, Individual> map;
    protected Map<Integer, int[]> inverseHash;
    protected Map<Integer, Integer> hits;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.batchSize = super.individuals.length;

        int initial = state.parameters.getInt(base.push(P_INITIAL_BATCH), defaultBase().push(P_INITIAL_BATCH));
        this.individuals = new Individual[initial]; // the super resize method is bugged

        this.behaviourIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), defaultBase().push(P_BEHAVIOUR_INDEX));
        this.resolution = state.parameters.getDouble(base.push(P_RESOLUTION), defaultBase().push(P_RESOLUTION));

        this.map = new ArrayListValuedHashMap<>();
        this.inverseHash = new HashMap<>();
        this.hits = new HashMap<>();
    }

    protected void updateRepertoire(EvolutionState state) {
        newInRepo = 0;
        for (Individual ind : super.individuals) {
            double[] behav = getBehaviourVector(state, ind);
            int[] bin = discretise(behav);
            int hash = hash(bin);
            Collection<Individual> binContents = map.get(hash);
            if (binContents.isEmpty()) {
                map.put(hash, (Individual) ind.clone());
                newInRepo++;
                inverseHash.put(hash, bin);
                hits.put(hash, 1);
            } else {
                // TODO: currently it supports only one element per bin
                Individual old = binContents.iterator().next();
                if (betterThan(ind, old)) {
                    map.remove(hash);
                    map.put(hash, (Individual) ind.clone());
                    newInRepo++;
                }
                hits.put(hash, hits.get(hash) + 1);                
            }
        }
    }

    protected boolean betterThan(Individual i1, Individual i2) {
        return ((ExpandedFitness) i1.fitness).getFitnessScore() > ((ExpandedFitness) i2.fitness).getFitnessScore();
    }

    protected double[] getBehaviourVector(EvolutionState state, Individual ind) {
        ExpandedFitness ef = (ExpandedFitness) ind.fitness;
        EvaluationResult eval = ef.getCorrespondingEvaluation(behaviourIndex);
        if (!(eval instanceof VectorBehaviourResult)) {
            state.output.fatal("Only VectorBehaviourResult's are supported. Got: " + eval.getClass().getCanonicalName() + " at index " + behaviourIndex);
        }
        VectorBehaviourResult vbr = (VectorBehaviourResult) eval;
        return vbr.getBehaviour();
    }

    protected int[] discretise(double[] v) {
        int[] r = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = (int) Math.floor(v[i] / resolution);
        }
        return r;
    }

    // TODO: does not guarantee zero collisions
    protected int hash(int[] array) {
        return Arrays.hashCode(array);
    }

    public MultiValuedMap<Integer, Individual> getRepertoire() {
        return map;
    }

    public int[] binFromHash(int h) {
        return inverseHash.get(h);
    }
    
    public int numHits(int h) {
        Integer get = hits.get(h);
        return get == null ? 0 : get;
    }
}
