/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class RangeBearingSensor extends AbstractSensor {

    private List<Object> objects;
    private boolean sort = false;
    private double range = Double.POSITIVE_INFINITY;
    private int objectCount;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double orientationNoise = 0;
    private double rangeNoise = 0;
    private int noiseType;

    public void setObjects(Collection<? extends Object> objs) {
        objects = new ArrayList<>(objs.size());
        for (Object o : objs) {
            if(o == null) {
                System.out.println("WARNING: NULL OBJECT");
            }
            if (o != ag) {
                objects.add(o);
            }
        }
        objectCount = objects.size();
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
        List<Pair<Double, Double>> readings = new ArrayList<>(objects.size());
        for (Object o : objects) {
            double dist = distFunction.agentToObjectDistance(ag, o);
            if(rangeNoiseAbs > 0) {
                dist += rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                dist = Math.max(dist, 0);
            }
            if (Double.isInfinite(range) || dist <= range) {
                Double2D point = (o instanceof Double2D) ? (Double2D) o : field.getObjectLocation(o);
                if (point != null) {
                    double angle = ag.angleTo(point);
                    if(orientationNoise > 0) {
                        angle += orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                        angle = EmboddiedAgent.normalizeAngle(angle);
                    }
                    readings.add(Pair.of(dist, angle));
                } else {
                    readings.add(Pair.of(Double.POSITIVE_INFINITY, 0d));
                }
            } else {
                readings.add(Pair.of(Double.POSITIVE_INFINITY, 0d));
            }
        }

        if (objectCount > 1 && sort) {
            Collections.sort(readings, new Comparator<Pair<Double, Double>>() {
                @Override
                public int compare(Pair<Double, Double> o1, Pair<Double, Double> o2) {
                    return Double.compare(o1.getLeft(), o2.getLeft());
                }
            });
        }

        double[] vals = new double[valueCount()];
        for (int i = 0; i < objectCount; i++) {
            if (i < readings.size()) {
                Pair<Double, Double> r = readings.get(i);
                vals[i * 2] = r.getLeft();
                vals[i * 2 + 1] = r.getRight();
            } else {
                vals[i * 2] = Double.POSITIVE_INFINITY;
                vals[i * 2 + 1] = 0;
            }
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

    @Override
    public void setAgent(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super.setAgent(state, field, ag);
        this.setObjects(objects); // quick fix so that the own agent can be excluded from the object list
    }

}
