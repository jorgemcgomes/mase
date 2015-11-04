/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.EvolutionState;
import ec.Subpopulation;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class StagedEvolution extends IncrementalEvolution {

    public static final String P_SEQUENCE = "sequence";
    protected int[] sequence;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String[] seq = state.parameters.getString(base.push(P_SEQUENCE), defaultBase().push(P_SEQUENCE)).split(",");
        if (seq.length != super.numStages) {
            state.output.fatal("Number of stages (" + numStages + ") is different from the sequence length (" + seq.length + ")");
        }
        sequence = new int[seq.length];
        for (int i = 0; i < seq.length; i++) {
            sequence[i] = Integer.parseInt(seq[i]);
        }
    }

    @Override
    public void changeStage(EvolutionState state, int stage) {
        super.changeStage(state, stage);
        updateFitness(state);
    }

    protected void updateFitness(EvolutionState state) {
        int next = sequence[currentStage];
        for (Subpopulation sub : state.population.subpops) {
            ExpandedFitness ef = (ExpandedFitness) sub.species.f_prototype;
            ef.setFitnessIndex(next);
        }
    }

}
