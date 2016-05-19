/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.Arrays;
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
    private Object[] closestObjects;
    private double[] lastDistances;

    private boolean ignoreRadius = false;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double orientationNoise = 0;
    private double rangeNoise = 0;
    private int noiseType;

    public void setArcs(double[] arcStart, double[] arcEnd) {
        if (arcStart.length != arcEnd.length) {
            throw new RuntimeException("Number of arc starts does not match arc ends. Starts: " + arcStart.length + " Ends: " + arcEnd.length);
        }
        this.arcStart = arcStart;
        this.arcEnd = arcEnd;
        this.closestObjects = new Object[valueCount()];
    }

    public void setArcs(int numArcs) {
        double[] start = new double[numArcs];
        double[] end = new double[numArcs];
        double arcAngle = (Math.PI * 2) / numArcs;
        start[0] = -arcAngle / 2; // first arc aligned with front
        end[0] = arcAngle / 2;
        for (int i = 1; i < numArcs; i++) {
            start[i] = end[i - 1];
            end[i] = start[i] + arcAngle;
            if (end[i] > Math.PI) {
                end[i] -= Math.PI * 2;
            }
        }
        this.setArcs(start, end);
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

    public void setObjectTypes(Class... types) {
        this.types = types;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public void ignoreRadius(boolean ignore) {
        this.ignoreRadius = ignore;
    }

    @Override
    public int valueCount() {
        return arcStart.length;
    }

    @Override
    public double[] readValues() {
        Bag neighbours = Double.isInfinite(range) ? field.allObjects
                : field.getNeighborsWithinDistance(ag.getLocation(), range + ag.getRadius(), false, true);
        lastDistances = new double[valueCount()];
        Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(closestObjects, null);
        if (range < 0.001) {
            return lastDistances;
        }
        double rangeNoiseAbs = Double.isInfinite(range) ? rangeNoise * fieldDiagonal : range * rangeNoise;
        for (Object n : neighbours) {
            if (objectMatch(n) && field.exists(n)) {
                double rawDist = distFunction.centerToCenterDistance(ag,n);
                double dist = ignoreRadius ? rawDist : distFunction.agentToObjectDistance(ag, n);
                if (rangeNoiseAbs > 0) {
                    dist += rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                    dist = Math.max(dist, 0);
                }
                if(!ignoreRadius && rawDist < distFunction.radius(n)) { // agent is inside the object
                    Arrays.fill(lastDistances, 0);
                    Arrays.fill(closestObjects, n);
                } else if (dist <= range) {
                    double angle = 0;
                    angle = ag.angleTo(field.getObjectLocation(n));
                    if (orientationNoise > 0) {
                        angle += orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                        angle = EmboddiedAgent.normalizeAngle(angle);
                    }
                    for (int a = 0; a < arcStart.length; a++) {
                        if ((angle >= arcStart[a] && angle <= arcEnd[a])
                                || (arcStart[a] > arcEnd[a] && (angle >= arcStart[a] || angle <= arcEnd[a]))) {
                            if (dist < lastDistances[a]) {
                                lastDistances[a] = dist;
                                closestObjects[a] = n;
                            }
                        }
                    }
                }
            }
        }
        return lastDistances;
    }

    public Object[] getClosestObjects() {
        return closestObjects;
    }

    public double[] getLastDistances() {
        return lastDistances;
    }

    protected boolean objectMatch(Object o) {
        if (o == ag) {
            return false;
        }
        for (Class type : types) {
            if (type.isInstance(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        double[] norm = new double[vals.length];
        double max = Double.isInfinite(range) ? fieldDiagonal : range;
        for (int i = 0; i < vals.length; i++) {
            if (binary) {
                norm[i] = Double.isInfinite(vals[i]) ? -1 : 1;
            } else {
                norm[i] = Double.isInfinite(vals[i]) ? 1 : vals[i] / max * 2 - 1;
            }
        }
        return norm;
    }
}
