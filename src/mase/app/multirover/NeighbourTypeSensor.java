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
public class NeighbourTypeSensor extends AbstractSensor {

    private final DistanceSensorArcs roverSensor;
    private final int type;

    NeighbourTypeSensor(DistanceSensorArcs roverSensor, int type) {
        super(roverSensor.getSimState(), roverSensor.getField(), roverSensor.getAgent());
        this.roverSensor = roverSensor;
        this.type = type;
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
        if (closestRover != null && closestRover.getActuatorType() == type) {
            return new double[]{1};
        } else {
            return new double[]{-1};
        }
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        return vals;
    }
}
