/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import mase.controllers.AgentController;

/**
 *
 * @author Jorge
 */
public class GoPlayerBoardEvaluation extends GoPlayer {

    public GoPlayerBoardEvaluation(Go sim, AgentController ac, int color) {
        super(sim, ac, color);
    }

    @Override
    protected GoState pickNextState(GoState currentState) {
       // Generate possible next states
        GoState[] possibleStates = currentState.possibleStates(color);
        double[] scores = new double[possibleStates.length];

        // Evaluate each of them
        for (int i = 0; i < possibleStates.length; i++) {
            int[] gridArray = possibleStates[i].getBoardDescription();
            double[] input = new double[gridArray.length];
            for (int x = 0; x < gridArray.length; x++) {
                if (gridArray[x] == GoState.EMPTY) {
                    input[x] = 0;
                } else if (gridArray[x] == color) { // the player stones are always represented with 1
                    input[x] = 1;
                } else { // and the opposite stones with -1
                    input[x] = -1;
                }
            }
            double[] output = ac.processInputs(input);
            scores[i] = output[0];
        }

        // Pick the one with highest outcome
        int highest = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[highest]) {
                highest = i;
            }
        }
        
        return possibleStates[highest];
    }
}
