/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public abstract class AbstractEffector implements Effector {

    protected final SimState state;
    protected final Continuous2D field;
    protected final EmboddiedAgent ag;

    public AbstractEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        this.state = state;
        this.field = field;
        this.ag = ag;
    }
    
    public SimState getSimState() {
        return state;
    }
    
    public Continuous2D getField() {
        return field;
    }
    
    public EmboddiedAgent getAgent() {
        return ag;
    }    

}
