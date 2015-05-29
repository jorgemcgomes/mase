/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package ec.coevolve;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.Subpopulation;
import ec.coevolve.MultiPopCoevolutionaryEvaluator2;
import ec.util.Parameter;
import ec.util.QuickSort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Multi-threaded version
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ForceRandom extends MultiPopCoevolutionaryEvaluator2 {

    public static final String P_SUBPOP_INDEX = "subpop-index";
    protected int subpopIndex;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.subpopIndex = state.parameters.getInt(base.push(P_SUBPOP_INDEX), null);
    }

    @Override
    void loadElites(EvolutionState state, int whichSubpop) {
        Subpopulation subpop = state.population.subpops[whichSubpop];
        if (whichSubpop == subpopIndex) {
            // Update hall of fame
            if (hallOfFame != null) {
                int best = 0;
                Individual[] oldinds = subpop.individuals;
                for (int x = 1; x < oldinds.length; x++) {
                    if (betterThan(oldinds[x], oldinds[best])) {
                        best = x;
                    }
                }
                hallOfFame[whichSubpop].add((Individual) subpop.individuals[best].clone());
            }

            // Random champions
            // Choose random positions
            int index = 0;
            ArrayList<Integer> pos = new ArrayList<Integer>(hallOfFame[whichSubpop].size());
            for (int i = 0; i < hallOfFame[whichSubpop].size(); i++) {
                pos.add(i);
            }
            Collections.shuffle(pos);
            for (int i = 0; i < pos.size() && i < numElite; i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(pos.get(i)).clone();
            }

            int toFill = numElite - index;
            if (toFill >= 1) {
                Individual[] orderedPop = Arrays.copyOf(subpop.individuals, subpop.individuals.length);
                QuickSort.qsort(orderedPop, new EliteComparator2());
                for (int j = 0; j < toFill; j++) {
                    eliteIndividuals[whichSubpop][index++] = (Individual) orderedPop[j].clone();
                }
            }

        } else {
            super.loadElites(state, whichSubpop);
        }
    }
}
