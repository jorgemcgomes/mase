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

    public void setObjects(Collection<? extends Object> objs) {
        objects = new ArrayList<Object>(objs.size());
        for (Object o : objs) {
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

    @Override
    public int valueCount() {
        return objectCount * 2;
    }

    @Override
    public double[] readValues() {
        List<Pair<Double, Double>> readings = new ArrayList<Pair<Double, Double>>(objects.size());
        for (Object o : objects) {
            double dist = Double.POSITIVE_INFINITY;
            double angle = 0;
            if (o instanceof Double2D) {
                Double2D p = (Double2D) o;
                dist = ag.distanceTo(p);
                angle = ag.angleTo(p);
            } else {
                Double2D p = field.getObjectLocation(o);
                if (p != null) {
                    double d = distFunction.distance(ag, o);
                    if(Double.isInfinite(range) || dist <= range) {
                        dist = d;
                        angle = ag.angleTo(p);
                    }
                }
            }
            readings.add(Pair.of(dist, angle));
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

}
