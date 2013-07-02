/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.GroupController;
import mase.mason.MasonSimulator;
import sim.display.GUIState;
import sim.engine.SimState;

/**
 *
 * @author jorge
 */
public class AggregationSimulator extends MasonSimulator {

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    @Override
    public SimState createSimState(GroupController gc, long seed) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
