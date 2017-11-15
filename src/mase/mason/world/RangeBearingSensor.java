/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.apache.commons.math3.util.MathUtils;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class RangeBearingSensor extends AbstractSensor {

    private WorldObject[] objects;
    private boolean sort = false;
    private double range = Double.POSITIVE_INFINITY;
    private int objectCount;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double orientationNoise = 0;
    private double rangeNoise = 0;
    private int noiseType;

    public RangeBearingSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    public void setObjects(Collection<? extends WorldObject> objs) {
        objects = new WorldObject[objs.size()];
        int index = 0;
        for (WorldObject o : objs) {
            if (o == null) {
                System.out.println("WARNING: NULL OBJECT");
            }
            if (o != ag) {
                objects[index++] = o;
            }
        }
        if(index < objects.length) {
            objects = Arrays.copyOf(objects, index);
        }
        objectCount = objects.length;
    }

    public void setObjectCount(int count) {
        this.objectCount = count;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public void setRange(double range) {
        this.range = range;
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
        return objectCount * 2;
    }

    @Override
    public double[] readValues() {
        double rangeNoiseAbs = Double.isInfinite(range) ? rangeNoise * fieldDiagonal : range * rangeNoise;

        final double[] distanceReadings = new double[objects.length];
        final double[] angleReadings = new double[objects.length];
        Integer[] indexes = new Integer[objects.length];

        int index = 0;
        for (WorldObject o : objects) {
            double dist = ag.distanceTo(o);
            if (rangeNoiseAbs > 0) {
                dist += rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                dist = Math.max(dist, 0);
            }
            if ((Double.isInfinite(range) || dist <= range) && o.getLocation() != null) {
                double angle = ag.angleTo(o.getLocation());
                if (orientationNoise > 0) {
                    angle += orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                    angle = MathUtils.normalizeAngle(angle, 0);
                }
                distanceReadings[index] = dist;
                angleReadings[index] = angle;
            } else {
                distanceReadings[index] = Double.POSITIVE_INFINITY;
                angleReadings[index] = 0d;
            }
            indexes[index] = index;
            index++;
        }

        if (objects.length > 1 && sort) {
            Arrays.sort(indexes, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(distanceReadings[o1], distanceReadings[o2]);
                }
            });
        }

        double[] vals = new double[valueCount()];
        for (int i = 0; i < objectCount; i++) {
            vals[i * 2] = distanceReadings[indexes[i]];
            vals[i * 2 + 1] = angleReadings[indexes[i]];
        }
        return vals;
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        double[] norm = new double[vals.length];
        double max = Double.isInfinite(range) ? fieldDiagonal : range;
        for (int i = 0; i < vals.length; i += 2) {
            norm[i] = Double.isInfinite(vals[i]) ? 1 : vals[i] / max * 2 - 1;
        }
        for (int i = 1; i < vals.length; i += 2) {
            norm[i] = vals[i] / Math.PI;
        }
        return norm;
    }
}
