/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.util.List;
import sim.engine.SimState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MaseSimState extends SimState {

    public MaseSimState(long seed) {
        super(seed);
    }
    
    public abstract Object getField();
    
    public abstract List<? extends SmartAgent> getSmartAgents();
    
}
