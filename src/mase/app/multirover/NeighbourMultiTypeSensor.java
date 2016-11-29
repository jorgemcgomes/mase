/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import mase.mason.world.AbstractSensor;
import mase.mason.world.DistanceSensorArcs;

/**
 *
 * @author jorge
 */
public class NeighbourMultiTypeSensor extends AbstractSensor {

    private final DistanceSensorArcs roverSensor;

    NeighbourMultiTypeSensor(DistanceSensorArcs roverSensor) {
        super(roverSensor.getSimState(), roverSensor.getField(), roverSensor.getAgent());
        this.roverSensor = roverSensor;
    }

    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public double[] readValues() {
        Object[] rovers = roverSensor.getClosestObjects();
        double[] dists = roverSensor.getLastDistances();
        double closestDist = Double.POSITIVE_INFINITY;
        Rover closestRover = null;
        for (int i = 0; i < rovers.length; i++) {
            if (rovers[i] != null && dists[i] < closestDist) {
                closestDist = dists[i];
                closestRover = (Rover) rovers[i];
            }
        }
        if (closestRover != null) {
            return new double[]{closestRover.getActuatorType()};
        } else {
            return new double[]{0};
        }
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        MultiRover mr = (MultiRover) state;
        return mr.par.numActuators == 1 ? vals
                : new double[]{-1 + vals[0] * (2.0 / (mr.par.numActuators - 1))};
    }
}
