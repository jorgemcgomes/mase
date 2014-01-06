/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.AgentController;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author jorge
 */
public abstract class GoPlayer implements Steppable {

    protected final int color;
    protected final AgentController ac;
    protected final Go sim;

    protected GoPlayer(Go sim, AgentController ac, int color) {
        this.ac = ac;
        this.color = color;
        this.sim = sim;
    }

    @Override
    public void step(SimState state) {
        GoState nextState = pickNextState(sim.state);

        if (sim.history.size() == 2) {
            // check for loops
            if (sim.history.getFirst().equals(nextState)) {
                sim.kill();
            }
            // check for double-passes
            if (nextState.equals(sim.history.getFirst()) && nextState.equals(sim.history.getLast())) {
                sim.kill();
            }
            // remove oldest in history
            sim.history.removeFirst();
        }
        try {
            // add next state to history
            sim.history.addLast(nextState.clone());
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GoPlayerBoardEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Make the move
        sim.state = nextState;
        if (sim.grid != null) {
            sim.grid.setTo(sim.state.asGrid2D());
        }
    }

    protected abstract GoState pickNextState(GoState currentState);

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
