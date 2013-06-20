/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.GroupController;
import mase.mason.MasonSimulator;
import sim.display.GUIState;
import sim.engine.SimState;

/**
 *
 * @author Jorge
 */
public class KeepawaySimulator extends MasonSimulator {

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
    }

    @Override
    public SimState createSimState(GroupController gc, long seed) {
        return new Keepaway(gc, seed);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public class Keepaway extends SimState {

        public Keepaway(GroupController gc, long seed) {
            super(seed);
        }
        
        
        
    }
    
}
