/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import mase.mason.world.AbstractEffector;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class RockEffector extends AbstractEffector {

    public static final double ACTIVATION_THRESHOLD = 0.5;
    public static final int NO_ACTIVATION = -1;
    public static final Color NOACTUATOR_COLOUR = Color.BLACK;
    public static final Color ACTUATING_COLOUR = Color.RED;

    public RockEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    public int valueCount() {
        return ((MultiRover) state).par.numActuators;
    }

    @Override
    public void action(double[] values) {
        Rover rover = (Rover) ag;
        rover.actuatorType = NO_ACTIVATION;
        double highestActivation = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > ACTIVATION_THRESHOLD && values[i] > highestActivation) {
                highestActivation = values[i];
                rover.actuatorType = i;
            }
        }
        rover.setColor(rover.actuatorType == NO_ACTIVATION ? Color.GRAY : Color.BLACK);
    }

}
