/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 * @author jorge
 */
public class PolygonRaySensor extends AbstractSensor {

    private Double2D[] rayStarts, rayEnds;
    private boolean binary = false;
    private double range;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double rangeNoise = 0;
    private double orientationNoise = 0;
    private int noiseType;
    private double[] angles;
    private Collection<StaticPolygonObject> polygons;

    public PolygonRaySensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    public void setRays(double range, int numRays, boolean frontAligned) {
        double increment = Math.PI * 2 / numRays;
        double start = frontAligned ? 0 : -increment / 2;
        angles = new double[numRays];
        for (int i = 0; i < numRays; i++) {
            angles[i] = start + increment * i;
        }
        this.setRays(range, angles);
    }

    /*
    If not set, or set to null, it will search for existing polygons every timestep
     */
    public void setObjects(Collection<StaticPolygonObject> polygons) {
        this.polygons = polygons;
    }

    public void setRays(double range, double... angles) {
        this.range = range;
        this.angles = angles;
        if (Double.isInfinite(range)) {
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

    /**
     * @param rangeNoise In percentage, relative to current range
     * @param orientationNoise In radians
     * @param type Uniform (0) or Gaussian (1)
     */
    public void setNoise(double rangeNoise, double orientationNoise, int type) {
        this.rangeNoise = rangeNoise;
        this.orientationNoise = orientationNoise;
        this.noiseType = type;
    }

    @Override
    public int valueCount() {
        return rayStarts.length;
    }

    @Override
    // ONLY WORKS FOR StaticPolygons
    public double[] readValues() {
        double[] vals = new double[valueCount()];
        if (binary) {
            Arrays.fill(vals, 0);
        } else {
            Arrays.fill(vals, Double.POSITIVE_INFINITY);
        }
        double rangeNoiseAbs = Double.isInfinite(range) ? rangeNoise * fieldDiagonal : range * rangeNoise;
        for (int i = 0; i < rayStarts.length; i++) {
            Double2D rs = rayStarts[i].rotate(ag.orientation2D()).add(ag.getLocation());
            Double2D re;
            if (rangeNoise > 0) {
                double newRange = range + rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                newRange = Math.max(0, newRange);
                double newOrientation = angles[i] + orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                re = new Double2D(ag.getRadius() + newRange, 0).rotate(newOrientation + ag.orientation2D()).add(ag.getLocation());
            } else {
                re = rayEnds[i].rotate(ag.orientation2D()).add(ag.getLocation());
            }

            for (StaticPolygonObject p : getCandidates()) {
                double dist = p.closestDistance(rs, re);
                if (!Double.isInfinite(dist)) {
                    vals[i] = binary ? 1 : Math.min(vals[i], dist);
                }
            }
        }
        return vals;
    }

    protected Collection<StaticPolygonObject> getCandidates() {
        if (polygons != null) {
            return polygons;
        } else {
            Collection<StaticPolygonObject> p = new ArrayList<>();
            for (Object o : field.allObjects) {
                if (o instanceof StaticPolygonObject) {
                    p.add((StaticPolygonObject) o);
                }
            }
            return p;
        }
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        double[] norm = new double[vals.length];
        double max = Double.isInfinite(range) ? fieldDiagonal : range;
        for (int i = 0; i < vals.length; i++) {
            if (binary) {
                norm[i] = vals[i] == 1 ? 1 : -1;
            } else {
                norm[i] = Double.isInfinite(vals[i]) ? 1 : (vals[i] / max) * 2 - 1;
            }
        }
        return norm;
    }
}
