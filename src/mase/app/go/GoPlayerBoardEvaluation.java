/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import mase.controllers.AgentController;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author Jorge
 */
public class GoPlayerBoardEvaluation implements Steppable {

    private int color;
    private AgentController ac;
    private Go sim;

    public GoPlayerBoardEvaluation(Go sim, AgentController ac, int color) {
        this.ac = ac;
        this.color = color;
        this.sim = sim;
    }

    @Override
    public void step(SimState state) {
        // Generate possible next states
        GoState[] possibleStates = sim.state.possibleStates(color);
        double[] scores = new double[possibleStates.length];
        
        // Evaluate each of them
        for(int i = 0 ; i < possibleStates.length ; i++) {
            int[] gridArray = possibleStates[i].getGrid().toArray();
            double[] input = new double[gridArray.length];
            for(int x = 0 ; x < gridArray.length ; x++) {
                if(gridArray[x] == GoState.EMPTY) {
                    input[x] = 0;
                } else if(gridArray[x] == color) { // the player stones are always represented with 1
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
        for(int i = 1 ; i < scores.length ; i++) {
            if(scores[i] > scores[highest]) {
                highest = i;
            }
        }
        
        // Make the move
        sim.state.update(possibleStates[highest]);
        
        // Ends if both players passed
        if(sim.state.passCount > 2) {
            sim.kill();
        }
        
        /* debugging        
         int random = state.random.nextInt(possibleStates.length);
         GoState move = possibleStates[random];
         this.sim.state.update(move);
        
         for (Group g : move.groups[color]) {
         System.out.print("Stones: ");
         for (Int2D p : g.stones) {
         System.out.print(p + " ");
         }
         System.out.println();
         System.out.print("Liberties: ");
         for (Int2D p : g.liberties) {
         System.out.print(p + " ");
         }
         System.out.println();
         }*/
    }
}
