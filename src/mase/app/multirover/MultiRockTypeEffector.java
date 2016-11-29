/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import java.util.List;
import mase.mason.world.AbstractEffector;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class MultiRockTypeEffector extends AbstractEffector {
    
    public static final double ACTIVATION_THRESHOLD = 0.5;
    public static final int NO_ACTIVATION = -1;
    public static final Color NOACTUATOR_COLOUR = Color.BLACK;
    public static final Color ACTUATING_COLOUR = Color.RED;
    private long lastActivationTime = Long.MIN_VALUE;
    private final long minActivationTime;
    
    public MultiRockTypeEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
        this.minActivationTime = ((MultiRover) state).par.minActivationTime;
    }

    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public void action(double[] values) {
        Rover rover = (Rover) ag;
        if(rover.getActuatorType() != NO_ACTIVATION && state.schedule.getSteps() - lastActivationTime < minActivationTime) {
            // locked in actuator, no change is allowed
            return;
        }
        
        MultiRover mr = (MultiRover) state;
        int oldType = rover.getActuatorType();
        // act1 in [0,1[ ; act2 in [1,2[ ; act3 in [2,3[ ; etc
        // The min is to protect against the (extremely unlikely) case where values[0]==1.0
        rover.setActuatorType(Math.min(mr.par.numActuators - 1, (int) (values[0] * mr.par.numActuators)));

        if(oldType != rover.getActuatorType()) {
            // changed actuator, update time stamp
            lastActivationTime = state.schedule.getSteps();
        }
    } 
}
