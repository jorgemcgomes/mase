/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import org.apache.commons.lang3.ArrayUtils;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class SelectiveRockSensor extends DistanceSensorArcs {

    public SelectiveRockSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    protected Collection<? extends Object> getCandidates() {
        Rover r = (Rover) ag;
        if (r.actuatorType == RockEffector.NO_ACTIVATION) {
            return Collections.EMPTY_LIST;
        } else {
            Collection<? extends Object> candidates = super.getCandidates();
            Iterator<? extends Object> iter = candidates.iterator();
            while (iter.hasNext()) {
                Rock next = (Rock) iter.next();
                boolean match = ArrayUtils.contains(next.getType().actuators, r.actuatorType);
                if (!match) {
                    iter.remove();
                }
            }
            return candidates;
        }
    }
}
