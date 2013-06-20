/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import sim.engine.SimState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class PortrayableSimState extends SimState {

    public PortrayableSimState(long seed) {
        super(seed);
    }
    
    public abstract Object getField();
    
}
