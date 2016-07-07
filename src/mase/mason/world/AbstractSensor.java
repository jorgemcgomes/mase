/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import net.jafama.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public abstract class AbstractSensor implements Sensor {

    protected final SimState state;
    protected final Continuous2D field;
    protected final EmboddiedAgent ag;
    protected final double fieldDiagonal;
    protected final GenericDistanceFunction distFunction;
    
    public AbstractSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        this.state = state;
        this.field = field;
        this.ag = ag;
        this.distFunction = new GenericDistanceFunction(field);
        this.fieldDiagonal = FastMath.sqrtQuick(FastMath.pow2(field.height) + FastMath.pow2(field.width));        
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
