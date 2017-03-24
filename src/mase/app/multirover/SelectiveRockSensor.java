/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.Collection;
import java.util.Collections;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import sim.field.continuous.Continuous2D;
import mase.mason.world.WorldObject;

/**
 *
 * @author jorge
 */
public class SelectiveRockSensor extends DistanceSensorArcs {
    

    public SelectiveRockSensor(MultiRover state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    protected Collection<? extends WorldObject> getCandidates() {
        Rover r = (Rover) ag;
        if (r.getActuatorType() == RockEffector.NO_ACTIVATION) {
            return Collections.EMPTY_LIST;
        } else {
            return ((MultiRover) state).matchingRocks[r.getActuatorType()];
        }
    }
}
