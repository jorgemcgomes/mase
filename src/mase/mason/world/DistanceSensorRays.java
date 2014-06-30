/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.Arrays;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class DistanceSensorRays extends AbstractSensor {

    private Double2D[] rayStarts, rayEnds;
    private boolean binary = false;
    private double range;

    public void setRays(double range, int numRays, boolean frontAligned) {
        double increment = Math.PI * 2 / numRays;
        double start = frontAligned ? 0 : -increment / 2;
        double[] angles = new double[numRays];
        for (int i = 0; i < numRays; i++) {
            angles[i] = start + increment * i;
        }
        this.setRays(range, angles);
    }

    public void setRays(double range, double... angles) {
        this.range = range;
        if(Double.isInfinite(range)) {
            range = fieldDiagonal;
        }
        
        rayStarts = new Double2D[angles.length];
        rayEnds = new Double2D[angles.length];

        Double2D baseStart = new Double2D(ag.getRadius(), 0);
        Double2D baseEnd = new Double2D(ag.getRadius() + range, 0);

        for (int i = 0; i < angles.length; i++) {
            rayStarts[i] = baseStart.rotate(angles[i]);
            rayEnds[i] = baseEnd.rotate(angles[i]);
        }
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    @Override
    public int valueCount() {
        return rayStarts.length;
    }

    @Override
    public double[] readValues() {
        double[] vals = new double[valueCount()];
        if (binary) {
            Arrays.fill(vals, 0);
        } else {
            Arrays.fill(vals, Double.POSITIVE_INFINITY);
        }
        for (int i = 0; i < rayStarts.length; i++) {
            Double2D rs = rayStarts[i].rotate(ag.orientation2D()).add(ag.getLocation());
            Double2D re = rayEnds[i].rotate(ag.orientation2D()).add(ag.getLocation());
            for (Object o : field.allObjects) {
                if (o instanceof StaticPolygon) {
                    StaticPolygon pe = (StaticPolygon) o;
                    double dist = pe.closestDistance(rs, re);
                    if (!Double.isInfinite(dist)) {
                        vals[i] = binary ? 1 : Math.min(vals[i], dist);
                    }
                }
            }
        }
        return vals;
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        double[] norm = new double[vals.length];
        double max = Double.isInfinite(range) ? fieldDiagonal : range;
        for (int i = 0; i < vals.length; i++) {
            if(binary) {
                norm[i] = vals[i] == 1 ? 1 : -1;
            } else {
                norm[i] = Double.isInfinite(vals[i]) ? 1 : (vals[i] / max) * 2 - 1;
            }
        }
        return norm;
    }
}
