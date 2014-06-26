/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.mason.world.SmartAgent;
import java.util.List;
import sim.engine.SimState;
import sim.portrayal.FieldPortrayal2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class GUICompatibleSimState extends SimState {

    public GUICompatibleSimState(long seed) {
        super(seed);
    }
    
    public abstract FieldPortrayal2D createFieldPortrayal();
    
    public abstract void setupPortrayal(FieldPortrayal2D port);
        
}
