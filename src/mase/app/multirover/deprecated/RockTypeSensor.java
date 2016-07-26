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
public class RockTypeSensor extends AbstractSensor {

    private final DistanceSensorArcs rockSensor;
    private final RockType type;

    RockTypeSensor(DistanceSensorArcs rockSensor, RockType type) {
        super(rockSensor.getSimState(), rockSensor.getField(), rockSensor.getAgent());
        this.rockSensor = rockSensor;
        this.type = type;
    }

    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public double[] readValues() {
        Object[] rocks = rockSensor.getClosestObjects();
        double[] dists = rockSensor.getLastDistances();
        double minDist = Double.POSITIVE_INFINITY;
        Rock closestRock = null;
        for (int i = 0; i < rocks.length; i++) {
            if (rocks[i] != null && dists[i] < minDist) {
                minDist = dists[i];
                closestRock = (Rock) rocks[i];
            }
        }
        if (closestRock != null && closestRock.getType() == type) {
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
