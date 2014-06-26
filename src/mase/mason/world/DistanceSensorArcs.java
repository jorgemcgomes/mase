/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.Arrays;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;

/**
 *
 * @author jorge
 */
public class DistanceSensorArcs extends AbstractSensor {

    private double[] arcStart;
    private double[] arcEnd;
    private double range = Double.POSITIVE_INFINITY;
    private Class[] types = new Class[]{Object.class};
    private boolean binary = false;

    public void setArcs(double[] arcStart, double[] arcEnd) {
        if (arcStart.length != arcEnd.length) {
            throw new RuntimeException("Number of arc starts does not match arc ends. Starts: " + arcStart.length + " Ends: " + arcEnd.length);
        }
        this.arcStart = arcStart;
        this.arcEnd = arcEnd;
    }

    public void setArcs(int numArcs) {
        arcStart = new double[numArcs];
        arcEnd = new double[numArcs];
        double arcAngle = (Math.PI * 2) / numArcs;
        arcStart[0] = -arcAngle / 2; // first arc aligned with front
        arcEnd[0] = arcAngle / 2;
        for (int i = 1; i < numArcs; i++) {
            arcStart[i] = arcEnd[i - 1];
            arcEnd[i] = arcStart[i] + arcAngle;
            if (arcEnd[i] > Math.PI) {
                arcEnd[i] -= Math.PI * 2;
            }
        }
    }

    public void setRange(double range) {
        this.range = range;
    }

    public void setObjectTypes(Class... types) {
        this.types = types;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    @Override
    public int valueCount() {
        return arcStart.length;
    }

    @Override
    public double[] readValues() {
        Bag neighbours = Double.isInfinite(range) ? field.allObjects
                : field.getNeighborsWithinDistance(ag.getLocation(), range + ag.getRadius(), false, true);
        double[] vals = new double[valueCount()];
        if (binary) {
            Arrays.fill(vals, 0);
        } else {
            Arrays.fill(vals, Double.POSITIVE_INFINITY);
        }
        for (Class type : types) {
            for (Object n : neighbours) {
                if (n != ag && type.isInstance(n)) {
                    double dist = distFunction.distance(ag, n);
                    if (dist <= range) {
                        double angle = ag.angleTo(field.getObjectLocation(n));
                        for (int a = 0; a < arcStart.length; a++) {
                            if ((angle >= arcStart[a] && angle <= arcEnd[a])
                                    || (arcStart[a] > arcEnd[a] && (angle >= arcStart[a] || angle <= arcEnd[a]))) {
                                vals[a] = binary ? 1 : Math.min(vals[a], dist);
                            }
                        }
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
            if (binary) {
                norm[i] = vals[i] == 1 ? 1 : -1;
            } else {
                norm[i] = Double.isInfinite(vals[i]) ? 1 : vals[i] / max * 2 - 1;
            }
        }
        return norm;
    }
}
