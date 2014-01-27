/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CompositeFeature implements EnvironmentalFeature {

    private final EnvironmentalFeature[] features;

    public CompositeFeature(EnvironmentalFeature[] features) {
        this.features = features;
    }

    @Override
    public double distanceTo(Double2D position) {
        double min = Double.POSITIVE_INFINITY;
        for (EnvironmentalFeature ef : features) {
            min = Math.min(min, ef.distanceTo(position));
        }
        return min;
    }

    @Override
    public double[] getStateVariables() {
        return new double[0];
    }
}
