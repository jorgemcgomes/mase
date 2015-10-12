/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Subpopulation;
import mase.evaluation.CoevolutionaryEvaluator;
import ec.util.Parameter;
import mase.evaluation.MetaEvaluator;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.evaluation.StagedEvolution;

/**
 *
 * @author jorge
 */
public class HaltedStagedEvolution extends StagedEvolution {

    private Subpopulation backup;

    @Override
    public void preEvaluationStatistics(EvolutionState state) {
        super.preEvaluationStatistics(state);
        if (super.currentStage == super.numStages - 1 && backup != null) {
            state.population.subpops = new Subpopulation[]{backup, state.population.subpops[0]};
            backup = null;
            super.updateFitness(state);
            
            CoevolutionaryEvaluator base = (CoevolutionaryEvaluator) ((MetaEvaluator) state.evaluator).getBaseEvaluator();
            Individual[][] elites = base.getEliteIndividuals();
            Individual[][] newElites = new Individual[2][elites[0].length];
            for(int i = 0 ; i < elites[0].length ; i++) {
                Individual dummy = new DummyIndividual();
                dummy.fitness = (Fitness) state.population.subpops[0].species.f_prototype.clone();
                newElites[0][i] = dummy;
                newElites[1][i] = elites[0][i];
            }     
            base.setEliteIndividuals(newElites);
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        backup = state.population.subpops[0];
        state.population.subpops = new Subpopulation[]{state.population.subpops[1]};
    }
    
    public static class DummyIndividual extends Individual implements AgentControllerIndividual {

        @Override
        public AgentController decodeController() {
            return null;
        }

        @Override
        public boolean equals(Object ind) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public Parameter defaultBase() {
            return null;
        }
        
    }

}
