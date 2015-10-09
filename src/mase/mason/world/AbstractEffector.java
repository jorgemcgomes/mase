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

    protected SimState state;
    protected Continuous2D field;
    protected EmboddiedAgent ag;

    @Override
    public void setAgent(SimState state, Continuous2D field, EmboddiedAgent ag) {
        this.state = state;
        this.field = field;
        this.ag = ag;
    }

}
