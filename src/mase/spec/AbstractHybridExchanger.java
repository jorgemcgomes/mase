/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Exchanger;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.MultiPopCoevolutionaryEvaluatorThreaded;
import mase.evaluation.CompoundEvaluationResult;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jorge
 */
public class AbstractHybridExchanger extends Exchanger {

    // list of allocations, separated by comma (ex: 0,0,1,1,2,2) OR V_HOMOGENEOUS OR V_HETEROGENEOUS OR a single number with the number of pops (agents are equally divided among all pops)
    public static final String P_INITIAL_ALLOCATION = "initial-allocation";
    public static final String V_HOMOGENEOUS = "homogeneous";
    public static final String V_HETEROGENEOUS = "heterogeneous";
    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    private static final long serialVersionUID = 1L;
    int popSize, nAgents;
    int behaviourIndex;
    List<MetaPopulation> metaPops;
    // array with the length == number of agents, where each position indicates the agent-subpop allocation
    int[] allocations;
    int merges, splits;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        nAgents = state.parameters.getInt(new Parameter("pop.subpops"), null); // TODO: bad dependency
        allocations = new int[nAgents];
        behaviourIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), null);
                
        String initial = state.parameters.getString(base.push(P_INITIAL_ALLOCATION), null);

        if (initial.equalsIgnoreCase(V_HOMOGENEOUS)) {
            setInitialAllocation(1);
        } else if (initial.equalsIgnoreCase(V_HETEROGENEOUS)) {
            setInitialAllocation(nAgents);
        } else if (StringUtils.isNumeric(initial)) {
            int pops = Integer.parseInt(initial);
            setInitialAllocation(pops);
        } else {
            String[] split = initial.split("[\\s\\,\\-]+");
            if (split.length != nAgents) {
                state.output.fatal("Initial allocation (" + split.length + ") does not match the number of agents (" + nAgents + ").", base.push(P_INITIAL_ALLOCATION));
            }
            for (int i = 0; i < allocations.length; i++) {
                allocations[i] = Integer.parseInt(split[i].trim());
            }
        }
    }

    public void setInitialAllocation(int pops) {
        allocations = new int[nAgents];
        int div = nAgents / pops;
        int rem = nAgents % pops;

        int index = 0;
        for (int i = 0; i < pops; i++) {
            for (int j = 0; j < div; j++) {
                allocations[index++] = i;
            }
            if (rem > 0) {
                allocations[index++] = i;
                rem--;
            }
        }
    }

    /*
     * Generic hybrid exchanger mechanics, with support for foreign individuals
     */
    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        merges = 0;
        splits = 0;

        // initialization -- first time in each evolutionary run
        if (metaPops == null) {
            popSize = state.population.subpops.get(0).individuals.size();
            initializationProcess(state);
        } else {
            // Only allows one operation per generation
            // reason: its important to evaluate before making any more changes
            merges = mergeProcess(state);
            if (merges == 0) {
                splits = splitProcess(state);
            }
            if (merges > 0 || splits > 0) {
                updateRepresentatives(state);
            }
        }

        // Update allocations
        for (int i = 0; i < metaPops.size(); i++) {
            for (Integer ag : metaPops.get(i).agents) {
                allocations[ag] = i;
            }
        }

        // create new population with individuals from metapops
        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new ArrayList<>(metaPops.size());
        for (int i = 0; i < metaPops.size(); i++) {
            newPop.subpops.add(metaPops.get(i).pop);
        }
        return newPop;
    }

    /*
     Update after breeding and prepare populations for evaluation
     */
    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        // Update metapop with new individuals generated by the breeding process
        for (int i = 0; i < metaPops.size(); i++) {
            metaPops.get(i).pop = state.population.subpops.get(i);
            // Mark all individuals for re-evaluation
            // Needed since the non-bred pops are copied from previous gens
            for (int j = 0; j < state.population.subpops.get(i).individuals.size(); j++) {
                state.population.subpops.get(i).individuals.get(j).evaluated = false;
            }
        }

        // breeding phase has passed - update ages
        for (MetaPopulation mp : metaPops) {
            mp.age++;
        }

        return state.population;
    }

    protected void updateRepresentatives(EvolutionState state) {
        MultiPopCoevolutionaryEvaluatorThreaded base = state.evaluator instanceof MetaEvaluator ? 
                (MultiPopCoevolutionaryEvaluatorThreaded) ((MetaEvaluator) state.evaluator).getBaseEvaluator() : 
                (MultiPopCoevolutionaryEvaluatorThreaded) state.evaluator;
        Individual[][] newElites = new Individual[metaPops.size()][base.getEliteIndividuals()[0].length];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            List<Individual> allInds = sortedCopy(mp.pop.individuals);
            for (int j = 0; j < newElites[i].length; j++) {
                newElites[i][j] = (Individual) allInds.get(j).clone();
            }
        }
        base.setEliteIndividuals(newElites);
    }

    /*
     Initialization, merging and spliting algorithms
     */
    protected void initializationProcess(EvolutionState state) {
        metaPops = new ArrayList<>();
        for (int i = 0; i < state.population.subpops.size(); i++) {
            MetaPopulation mp = new MetaPopulation();
            mp.age = 0;
            for (int j = 0; j < allocations.length; j++) {
                if (allocations[j] == i) {
                    mp.agents.add(j);
                }
            }
            if (!mp.agents.isEmpty()) {
                mp.pop = state.population.subpops.get(i);
                metaPops.add(mp);
            }
        }
    }

    protected int mergeProcess(EvolutionState state) {
        return 0;
    }

    protected int splitProcess(EvolutionState state) {
        return 0;
    }

    protected static class MetaPopulation implements Serializable {

        private static final long serialVersionUID = 1L;

        List<Integer> agents;
        Subpopulation pop;
        int age;
        int lockDown;

        MetaPopulation() {
            this.agents = new ArrayList<>();
            this.age = 0;
        }

        @Override
        public String toString() {
            String s = "[";
            for (int i = 0; i < agents.size() - 1; i++) {
                s += agents.get(i) + ",";
            }
            if (agents.size() > 0) {
                s += agents.get(agents.size() - 1);
            }
            return s + "]";
        }
    }

    public int[] getAllocations() {
        return allocations;
    }

    protected List<MetaPopulation> getMetaPopulations() {
        return metaPops;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }

    /**
     * UTIL METHODS
     */
    /*
     Uses final scores
     Sorts from the highest fitness to the lowest fitness (i.e., highest fitness is in position 0)
     */
    protected static class FitnessComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
            return Double.compare(o2.fitness.fitness(), o1.fitness.fitness());
        }
    }

    protected static List<Individual> sortedCopy(List<Individual> inds) {
        List<Individual> copy = new ArrayList<>(inds);
        Collections.sort(copy, new FitnessComparator());
        return copy;
    }

    protected static List<Individual> getElitePortion(List<Individual> inds, int num) {
        inds = sortedCopy(inds);
        return inds.subList(0, num);
    }

    protected static BehaviourResult getAgentBR(Individual ind, int agent, int index) {
        ExpandedFitness nf = (ExpandedFitness) ind.fitness;
        CompoundEvaluationResult ser = (CompoundEvaluationResult) nf.getEvaluationResults()[index];
        return (BehaviourResult) ser.getEvaluation(agent);
    }

}
