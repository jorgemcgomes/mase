/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class BorderCoverageFitness extends CoverageFitness {

    public static final String P_BORDER_CELLS = "border-cells";
    private static final long serialVersionUID = 1L;
    private int borderCells;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        borderCells = state.parameters.getInt(base.push(P_BORDER_CELLS), null);
    }

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        double total = 0;
        int cellCount = 0;
        for (int i = 0; i < accum.length; i++) {
            for (int j = 0; j < accum.length; j++) {
                if (i < borderCells || i >= accum.length - borderCells || j < borderCells || j >= accum.length - borderCells) {
                    total += accum[i][j];
                    cellCount++;
                }
            }
        }
        return total / cellCount / currentEvaluationStep;
    }

}
