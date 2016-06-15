/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import mase.evaluation.BehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class StochasticHybridExchanger extends AbstractHybridExchanger {

    // algorithm parameter values
    public static final String P_MERGE_THRESHOLD = "merge-threshold";
    public static final String P_MAX_LOCKDOWN = "max-lockdown";

    // algorithm implementation modes
    public static final String P_DISTANCE_CALCULATOR = "distance-calculator";
    public static final String P_DISTANCE_ELITE = "distance-elite";
    public static final String P_MERGE_SELECTION = "merge-selection";
    public static final String P_MERGE_AGENTS = "merge-agents";
    public static final String P_SPLIT_AGENTS = "split-agents";

    private static final long serialVersionUID = 1L;

    public enum MergeSelection {

        truncate, fitnessproportionate, random
    }

    public enum MergeAgentsProportion {

        equal, largest, proportionate

    }

    public enum SplitAgentsProportion {

        one, half, random

    }

    double mergeThreshold;
    int maxLockdown;

    double distanceElite;
    MergeSelection mergeSelection;
    MergeAgentsProportion mergeProportion;
    SplitAgentsProportion splitProportion;

    DistanceCalculator distCalculator;
    double[][] distanceMatrix = new double[0][0];
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        mergeThreshold = state.parameters.getDouble(base.push(P_MERGE_THRESHOLD), null);
        maxLockdown = state.parameters.getInt(base.push(P_MAX_LOCKDOWN), null);
        distanceElite = state.parameters.getDouble(base.push(P_DISTANCE_ELITE), null);
        mergeSelection = MergeSelection.valueOf(state.parameters.getString(base.push(P_MERGE_SELECTION), null));
        mergeProportion = MergeAgentsProportion.valueOf(state.parameters.getString(base.push(P_MERGE_AGENTS), null));
        splitProportion = SplitAgentsProportion.valueOf(state.parameters.getString(base.push(P_SPLIT_AGENTS), null));
        distCalculator = (DistanceCalculator) state.parameters.getInstanceForParameter(base.push(P_DISTANCE_CALCULATOR), null, DistanceCalculator.class);
        distCalculator.setup(state, base.push(DistanceCalculator.P_BASE));
    }

    @Override
    protected void initializationProcess(EvolutionState state) {
        super.initializationProcess(state);
        for (MetaPopulation mp : metaPops) {
            mp.lockDown = calculateLockDown(mp, state);
        }
    }    
    
    protected int calculateLockDown(MetaPopulation pop, EvolutionState state) {
        return state.random[0].nextInt(maxLockdown);
    }
    
    
    /*****************
     * MERGE PROCESS
     *****************/    
    
    /* Similar to bottom-up (agglomerative) hierarchical clustering */
    @Override
    protected int mergeProcess(EvolutionState state) {
        distanceMatrix = computeMetapopDistances(state);

        Pair<MetaPopulation, MetaPopulation> nextMerge = findNextMerge(distanceMatrix, state);

        // Merge if they are similar
        if (nextMerge != null) {
            state.output.message("*** Merging " + nextMerge.getLeft() + " with " + nextMerge.getRight() + " ***");
            MetaPopulation mpNew = mergePopulations(nextMerge.getLeft(), nextMerge.getRight(), state);
            mpNew.lockDown = calculateLockDown(mpNew, state);
            metaPops.remove(nextMerge.getLeft());
            metaPops.remove(nextMerge.getRight());
            metaPops.add(mpNew);
            return 1;
        }
        return 0;
    }
    
    protected Pair<MetaPopulation, MetaPopulation> findNextMerge(double[][] distanceMatrix, EvolutionState state) {
        MetaPopulation closeI = null, closeJ = null;
        double closest = 0;
        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = i + 1; j < metaPops.size(); j++) {
                double d = distanceMatrix[i][j];
                if (metaPops.get(i).age >= metaPops.get(i).lockDown && metaPops.get(j).age >= metaPops.get(j).lockDown && (closeI == null || d < closest)) {
                    closeI = metaPops.get(i);
                    closeJ = metaPops.get(j);
                    closest = d;
                }
            }
        }
        if (closeI != null) {
            state.output.message("*** Closest: " + closeI + " " + closeJ + " -- " + closest + " ***");
        }
        if (closeI != null && closest <= mergeThreshold) {
            return Pair.of(closeI, closeJ);
        }
        return null;
    }

    protected MetaPopulation mergePopulations(MetaPopulation mp1, MetaPopulation mp2, EvolutionState state) {
        // Create new MetaPop
        MetaPopulation mpNew = new MetaPopulation();
        if (mp1.age > mp2.age) {
            mpNew.agents = new ArrayList<>(mp1.agents);
            mpNew.agents.addAll(mp2.agents);
        } else {
            mpNew.agents = new ArrayList<>(mp2.agents);
            mpNew.agents.addAll(mp1.agents);
        }

        if (mp1.agents.size() > mp2.agents.size()) {
            mpNew.pop = (Subpopulation) mp1.pop.emptyClone();
        } else {
            mpNew.pop = (Subpopulation) mp2.pop.emptyClone();
        }
        mpNew.pop.individuals = new Individual[mp1.pop.individuals.length];
        fillNewPopulation(mpNew, mp1, mp2, state);
        return mpNew;
    }    

    protected void fillNewPopulation(MetaPopulation mpNew, MetaPopulation mp1, MetaPopulation mp2, EvolutionState state) {
        // The number of individuals to pick from each pop
        int from1 = 0;
        if (mergeProportion == MergeAgentsProportion.equal) {
            from1 = mpNew.pop.individuals.length / 2;
        } else if (mergeProportion == MergeAgentsProportion.largest) {
            from1 = mp1.agents.size() >= mp2.agents.size() ? mpNew.pop.individuals.length : 0;
        } else if (mergeProportion == MergeAgentsProportion.proportionate) {
            from1 = Math.round((float) mp1.agents.size() / (mp1.agents.size() + mp2.agents.size()) * mpNew.pop.individuals.length);
        }
        int from2 = mpNew.pop.individuals.length - from1;

        Individual[] picked1 = selectIndividuals(mp1.pop.individuals, from1, mergeSelection, state);
        Individual[] picked2 = selectIndividuals(mp2.pop.individuals, from2, mergeSelection, state);
        System.arraycopy(picked1, 0, mpNew.pop.individuals, 0, picked1.length);
        System.arraycopy(picked2, 0, mpNew.pop.individuals, picked1.length, picked2.length);
    }    
    
    protected Individual[] selectIndividuals(Individual[] pool, int num, MergeSelection mode, EvolutionState state) {
        Individual[] picked = new Individual[num];
        if (mode == MergeSelection.truncate) {
            Individual[] sorted = sortedCopy(pool);
            System.arraycopy(sorted, 0, picked, 0, num);
        } else if (mode == MergeSelection.fitnessproportionate) {
            double total = 0;
            LinkedList<Individual> poolList = new LinkedList<>();
            for (Individual ind : pool) {
                poolList.add(ind);
                total += ((SimpleFitness) ind.fitness).fitness();
            }
            int index = 0;
            while (index < num) {
                double accum = 0;
                double rand = state.random[0].nextDouble() * total;
                Iterator<Individual> iter = poolList.iterator();
                while (iter.hasNext()) {
                    Individual ind = iter.next();
                    accum += ((SimpleFitness) ind.fitness).fitness();
                    if (accum >= rand) {
                        picked[index++] = ind;
                        iter.remove();
                        total -= ((SimpleFitness) ind.fitness).fitness();
                        break;
                    }
                }
            }
        } else if (mode == MergeSelection.random) {
            LinkedList<Individual> poolList = new LinkedList<>(Arrays.asList(pool));
            int index = 0;
            while (index < num) {
                int rand = state.random[0].nextInt(poolList.size());
                picked[index++] = poolList.get(rand);
                poolList.remove(rand);
            }
        } else {
            state.output.fatal("Unknown picking mode: " + mode);
        }
        return picked;
    }
    
    protected double[][] computeMetapopDistances(EvolutionState state) {
        // Retrieve agent behaviours, aggregated by MetaPopulation
        List<BehaviourResult>[] behavs = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            if (mp.age < mp.lockDown) {
                behavs[i] = Collections.EMPTY_LIST;
            } else {
                behavs[i] = new ArrayList<>();
                Individual[] elite = getElitePortion(mp.pop.individuals, (int) Math.ceil(distanceElite * popSize));
                for (Individual ind : elite) {
                    for (Integer a : mp.agents) {
                        behavs[i].add(getAgentBR(ind, a, behaviourIndex));
                    }
                }                
            }
        }
        return distCalculator.computeDistances(behavs, state);
    }


    /*****************
     * SPLIT PROCESS
     *****************/
    
    @Override
    protected int splitProcess(EvolutionState state) {
        // Find new splits
        MetaPopulation parent = findNextSplit(state);

        // Create new metapop
        if (parent != null) {
            MetaPopulation child = fork(parent, state);
            metaPops.add(child);
            parent.lockDown = calculateLockDown(parent, state);
            child.lockDown = parent.lockDown; // same lockdown to give them a chance to remerge before any further splits
            state.output.message("*** Spliting " + child + " from " + parent + " ***");
            return 1;
        }
        return 0;
    }

    protected MetaPopulation findNextSplit(EvolutionState state) {
        MetaPopulation chosen = null;
        for (MetaPopulation mp : metaPops) {
            if (mp.agents.size() > 1 && mp.age >= mp.lockDown && (chosen == null || mp.age > chosen.age)) {
                chosen = mp;
            }
        }
        if (chosen != null) {
            return chosen;
        }
        return null;
    }

    protected MetaPopulation fork(MetaPopulation parent, EvolutionState state) {
        List<Integer> forkAgents = new ArrayList<>();

        if (splitProportion == SplitAgentsProportion.one || parent.agents.size() <= 3) {
            forkAgents.add(parent.agents.get(0));
        } else if (splitProportion == SplitAgentsProportion.half) {
            int num = parent.agents.size() / 2;
            for (int i = 0; i < num; i++) {
                forkAgents.add(parent.agents.get(i));
            }
        } else if (splitProportion == SplitAgentsProportion.random) {
            int num = 1 + state.random[0].nextInt(parent.agents.size() / 2 - 1);
            for (int i = 0; i < num; i++) {
                forkAgents.add(parent.agents.get(i));
            }
        }

        parent.age = 0;
        parent.agents.removeAll(forkAgents);

        MetaPopulation child = new MetaPopulation();
        child.agents.addAll(forkAgents);
        child.pop = (Subpopulation) parent.pop.emptyClone();
        child.pop.individuals = new Individual[parent.pop.individuals.length];
        for (int k = 0; k < parent.pop.individuals.length; k++) {
            child.pop.individuals[k] = (Individual) parent.pop.individuals[k].clone();
        }
        return child;
    }
}
