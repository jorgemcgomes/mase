/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 *
 * @author jorge
 */
public abstract class AbstractEffector extends OvalPortrayal2D implements Effector {

    private static final long serialVersionUID = 1L;

    protected final SimState state;
    protected final Continuous2D field;
    protected final EmboddiedAgent ag;

    public AbstractEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(ag.getRadius() * 2);
        this.filled = false;
        this.paint = new Color(0,0,0,0);
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
