/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.util.Arrays;
import java.util.Comparator;
import mase.controllers.AgentController;
import sim.util.Int2D;

/**
 *
 * @author Jorge
 */
public class GoPlayerMoveEvaluation extends GoPlayer {

    public GoPlayerMoveEvaluation(Go sim, AgentController ac, int color) {
        super(sim, ac, color);
    }

    @Override
    protected GoState pickNextState(GoState currentState) {
        // Board representation
        int[] descr = currentState.getBoardDescription();
        double[] input = new double[descr.length];
        for (int x = 0; x < descr.length; x++) {
            if (descr[x] == GoState.EMPTY) {
                input[x] = 0;
            } else if (descr[x] == color) { // the player stones are always represented with 1
                input[x] = 1;
            } else { // and the opposite stones with -1
                input[x] = -1;
            }
        }

        // Run network
        final double[] output = ac.processInputs(input);

        // Choose next action
        Integer[] indexes = new Integer[output.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(output[o2], output[o1]);
            }

        });

        if (output[indexes[0]] > 0.2) { // if there is at least one output > 0.2
            // returns the first (highest) valid move
            for (Integer index : indexes) {
                Int2D move = GoState.reverseIndex(index);
                GoState s = currentState.nextState(color, move);
                if(s != null) { // if is valid move
                    return s;
                }
            }
        }
        return currentState;
    }

}
