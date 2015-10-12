/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import sim.engine.SimState;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class GUICompatibleSimState extends SimState {

    public GUICompatibleSimState(long seed) {
        super(seed);
    }
    
    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    public abstract void setupPortrayal(FieldPortrayal2D port);
        
}
