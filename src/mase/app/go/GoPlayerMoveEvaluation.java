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
public class GoPlayerMoveEvaluation implements Steppable {
        private int color;
    private AgentController ac;
    private Go sim;
    
    public GoPlayerMoveEvaluation(Go sim, AgentController ac, int color) {
        this.ac = ac;
        this.color = color;
        this.sim = sim;
    }

    @Override
    public void step(SimState state) {
        // Read board configuration
        //int[] descr = sim.board.getBoardDescription();
        double[] input = null;
        
        // Run network
        double[] output = ac.processInputs(input);
        
        // Choose action
        
        
        // Make action
        
        
        /*System.out.println("Stepping " + color + " ; Time: " + state.schedule.getTime());        
        int next = -1;
        while(next == -1) {
            int r = state.random.nextInt(25);
            System.out.println("Checking position " + r);
            boolean v = sim.board.isValidMove(r, color);
            System.out.println(v);
            if(sim.board.isValidMove(r, color)) {
                next = r;
            }
        }
        sim.board.makeMove(next, color);*/
    }
    
}
