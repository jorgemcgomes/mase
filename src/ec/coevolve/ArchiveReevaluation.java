/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.coevolve;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import java.util.Arrays;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.evaluation.ExpandedFitness;
import mase.novelty.NoveltyEvaluation;

/**
 *
 * @author jorge
 */
public class ArchiveReevaluation extends MultiPopCoevolutionaryEvaluatorExtra {

    @Override
    public void evaluatePopulation(EvolutionState state) {
        // Evaluate population
        super.evaluatePopulation(state);

        if (archives == null) {
            for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
                if (pe instanceof NoveltyEvaluation) {
                    NoveltyEvaluation ne = (NoveltyEvaluation) pe;
                    archives = ne.getArchives();
                    break;
                }
            }
        }

        // print pop evals
        /*System.out.println("POPULATION BEFORE REEVAL");
         for (int i = 0; i < state.population.subpops.length; i++) {
         for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
         System.out.println(i + "\t" + j + "\t" + ((ExpandedFitness) state.population.subpops[i].individuals[j].fitness).getFitnessScore());
         }
         }
         System.out.println("ARCHIVE BEFORE REEVAL");
         for (int i = 0; i < state.population.subpops.length; i++) {
         for (int j = 0; j < archives[i].size(); j++) {
         System.out.println(i + "\t" + j + "\t" + ((ExpandedFitness) archives[i].get(j).getIndividual().fitness).getFitnessScore());
         }
         }*/

        /*
         Reevaluate the novelty archives
         */
        // Create Population with the individuals from the archives
        Population archPop = (Population) state.population.emptyClone();
        for (int i = 0; i < archPop.subpops.length; i++) {
            archPop.subpops[i].individuals = new Individual[archives[i].size()];
            for (int j = 0; j < archPop.subpops[i].individuals.length; j++) {
                archPop.subpops[i].individuals[j] = archives[i].get(j).getIndividual();
            }
        }

        // Re-evaluate
        boolean[] asessFitness = new boolean[archPop.subpops.length];
        Arrays.fill(asessFitness, true);
        ((GroupedProblemForm) p_problem).preprocessPopulation(state, archPop, asessFitness, false);
        performCoevolutionaryEvaluation(state, archPop, (GroupedProblemForm) p_problem);
        ((GroupedProblemForm) p_problem).postprocessPopulation(state, archPop, asessFitness, false);

        // print pop evals
        /*System.out.println("POPULATION AFTER REEVAL");
         for (int i = 0; i < state.population.subpops.length; i++) {
         for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
         System.out.println(i + "\t" + j + "\t" + ((ExpandedFitness) state.population.subpops[i].individuals[j].fitness).getFitnessScore());
         }
         }
         System.out.println("ARCHIVE AFTER REEVAL");
         for (int i = 0; i < state.population.subpops.length; i++) {
         for (int j = 0; j < archives[i].size(); j++) {
         System.out.println(i + "\t" + j + "\t" + ((ExpandedFitness) archives[i].get(j).getIndividual().fitness).getFitnessScore());
         }
         }*/
    }
}
