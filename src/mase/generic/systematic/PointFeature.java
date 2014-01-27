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
public class PointFeature implements EnvironmentalFeature {

    private Double2D point;
    private double radius;

    public PointFeature(Double2D point) {
        this(point, 0);
    }

    public PointFeature(Double2D point, double radius) {
        this.point = point;
        this.radius = radius;
    }

    @Override
    public double distanceTo(Double2D position) {
        return Math.max(0, position.distance(point) - radius);
    }

    @Override
    public double[] getStateVariables() {
        return new double[0];
    }
}
