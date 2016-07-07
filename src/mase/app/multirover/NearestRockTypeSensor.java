/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.Arrays;
import mase.mason.world.AbstractSensor;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;

/**
 *
 * @author jorge
 */
public class NearestRockTypeSensor extends AbstractSensor {

    public NearestRockTypeSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    public int valueCount() {
        return ((MultiRover)state).par.usedTypes.size();
    }

    @Override
    public double[] readValues() {
        MultiRover mr = (MultiRover) state;
        Bag ns = mr.field.getNeighborsWithinDistance(ag.getLocation(), mr.par.rockSensorRange);
        double dmin = Double.POSITIVE_INFINITY;
        Rock closest = null;
        for(Object o : ns) {
            if(o instanceof Rock) {
                Rock r = (Rock) o;
                double d  = ag.getLocation().distance(r.getLocation());
                if(d < dmin) {
                    dmin = d;
                    closest = r;
                }
            }
        }
        double[] vals = new double[mr.par.usedTypes.size()];
        Arrays.fill(vals, -1);
        if(closest != null) {
            int index = 0;
            for(RockType t : mr.par.usedTypes) {
                if(closest.getType() == t) {
                    vals[index] = 1;
                    break;
                }
                index++;
            }
        }
        return vals;
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        return vals;
    }
    
}
