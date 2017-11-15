/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeGatherer;
import ec.gp.GPTreeConstraints;
import ec.gp.koza.KozaShortStatistics;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jorge
 */
public class ExtendedKozaStatistics extends KozaShortStatistics {

    private static final long serialVersionUID = 1L;
    private GPNodeGatherer[] gatherers;
    private List nodeNames;
    private double[] functionUsage;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        if(doSubpops) {
            state.output.fatal("Multiple subpops not currently supported by " + getClass().getCanonicalName());
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        GPIndividual sample = (GPIndividual) (state.population.subpops[0].individuals[0]);
        GPTreeConstraints constraints = sample.trees[0].constraints((GPInitializer) state.initializer);
        GPFunctionSet functionset = constraints.functionset;
        nodeNames = new ArrayList(functionset.nodesByName.keySet());
        Collections.sort(nodeNames);
        gatherers = new GPNodeGatherer[nodeNames.size()];
        for (int i = 0; i < gatherers.length; i++) {
            final GPNode node = ((GPNode[]) functionset.nodesByName.get(nodeNames.get(i)))[0];
            gatherers[i] = new GPNodeGatherer() {
                @Override
                public boolean test(final GPNode thisNode) {
                    return thisNode.name().equals(node.name());
                }
            };
        }
        functionUsage = new double[gatherers.length];
        
        /*
         * Add header 
         */
        String header = "Generation";
        if (doTime) {
            header += " BreedTime EvalTime";
        }
        if(doDepth) {
            header += " |D";
            for(int i = 0 ; i < sample.trees.length ; i++) {
                header += (sample.trees.length == 1 ? " MeanDepth" : "MeanDepth."+i);
            }
            header += " D|";
        }
        if(doSize) {
            header += " |S";
            for(int i = 0 ; i < sample.trees.length ; i++) {
                header += (sample.trees.length == 1 ? " MeanSize" : "MeanSize."+i);
            }
            header += " S|";
        }
        for(Object s : nodeNames) {
            header += " f." + s;
        }
        if(doSize) {
            header += " MeanSizeGen MeanSizeSoFar BestSizeGen BestSizeSoFar";
        }
        header +=  " MeanFitnessGen BestFitnessGen BestFitnessSoFar";
        state.output.println(header, statisticslog);
        
        super.postInitializationStatistics(state);
    }

    @Override
    protected void printExtraPopStatisticsBefore(EvolutionState state) {
        super.printExtraPopStatisticsBefore(state);
        Subpopulation sub = state.population.subpops[0];
        int[] count = new int[functionUsage.length];
        for (Individual ind : sub.individuals) {
            GPNode root = ((GPIndividual) ind).trees[0].child;
            for (int i = 0; i < gatherers.length; i++) {
                count[i] += root.numNodes(gatherers[i]);
            }
        }

        for (int i = 0; i < gatherers.length; i++) {
            state.output.print(count[i] / (double) sub.individuals.length + " ", statisticslog);
        }
    }

}
