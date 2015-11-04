/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Arrays;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.IncrementalEvolution;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;

/**
 *
 * @author jorge
 */
public class MOEAStagedEvolution extends IncrementalEvolution {

    public static final String P_SEQUENCE = "sequence";
    protected String[] sequence;    
    protected NSGA2 moea;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        sequence = state.parameters.getString(base.push(P_SEQUENCE), defaultBase().push(P_SEQUENCE)).split(",");
        if (sequence.length != super.numStages) {
            state.output.fatal("Number of stages (" + numStages + ") is different from the sequence length (" + sequence.length + ")");
        }
        MetaEvaluator e = (MetaEvaluator) state.evaluator;
        for(PostEvaluator pe : e.getPostEvaluators()) {
            if(pe instanceof NSGA2) {
                moea = (NSGA2) pe;
                break;
            }
        }
        if(moea == null) {
            state.output.fatal("No NSGA2 PostEvaluator was found");
        }
    }    

    @Override
    public void changeStage(EvolutionState state, int stage) {
        super.changeStage(state, stage);
        String[] seq = Arrays.copyOf(sequence, stage + 1);
        moea.include = seq;
    }

    @Override
    protected double countAboveFraction(EvolutionState state) {
        int above = 0;
        int all = 0;
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                ExpandedFitness ef = (ExpandedFitness) ind.fitness;
                boolean ab = true;
                for(int i = 0 ; i <= currentStage && ab ; i++) {
                    if(ef.getScore(sequence[i]) < fitnessThreshold[i]) {
                        ab = false;
                    }
                }
                if(ab) {
                    above++;
                }
                all++;
            }
        }
        return (double) above / all;
    }
}
