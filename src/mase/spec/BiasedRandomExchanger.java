/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.BehaviourResult;
import static mase.spec.AbstractHybridExchanger.P_BEHAVIOUR_INDEX;

/**
 *
 * @author jorge
 */
public class BiasedRandomExchanger extends RandomExchanger {

    private static final long serialVersionUID = 1L;
    public static final String P_DISTANCE_CALCULATOR = "distance-calculator";
    public static final String P_DISTANCE_ELITE = "distance-elite";

    DistanceCalculator distCalculator;
    double distanceElite;
    int behaviourIndex;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        distCalculator = (DistanceCalculator) state.parameters.getInstanceForParameter(base.push(P_DISTANCE_CALCULATOR), null, DistanceCalculator.class);
        distCalculator.setup(state, base.push(DistanceCalculator.P_BASE));
        distanceElite = state.parameters.getDouble(base.push(P_DISTANCE_ELITE), null);
        behaviourIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), null);

    }

    protected double[][] computeDistances(EvolutionState state) {
        List<BehaviourResult>[] behavs = new List[state.population.subpops.length];
        for (int i = 0; i < behavs.length; i++) {
            behavs[i] = new ArrayList<>();
            Individual[] elite = AbstractHybridExchanger.getElitePortion(state.population.subpops[i].individuals,
                    (int) Math.ceil(distanceElite * state.population.subpops[i].individuals.length));
            for (Individual ind : elite) {
                behavs[i].add(AbstractHybridExchanger.getAgentBR(ind, i, behaviourIndex));
            }
        }
        return distCalculator.computeDistances(behavs, state);
    }

    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        double[][] dists = computeDistances(state);
        
        for (int i = 0 ; i < state.population.subpops.length ; i++) {
            Subpopulation sub = state.population.subpops[i];
            int replace = (int) Math.round(sub.individuals.length * foreignProportion);
            double[] probs = dists[i];
            double total = 0;
            for(int j = 0 ; j < probs.length ; j++) {
                probs[j] = i == j ? 0 : 1 - probs[j];
                total += probs[j];
            }
            
            for (int j = 0; j < replace; j++) {
                // pick population
                Subpopulation pickSub = null;
                double rand = state.random[0].nextDouble() * total;
                for(int k = 0 ; k < probs.length ; k++) {
                     rand -= probs[k];
                     if(rand <= 0d) {
                         pickSub = state.population.subpops[k];
                         break;
                     }
                }

                // pick individual
                Individual ind = pickSub.individuals[state.random[0].nextInt(pickSub.individuals.length)];
                // replace
                sub.individuals[sub.individuals.length - replace - 1] = (Individual) ind.clone();
            }
        }
        return state.population;
    }

}
