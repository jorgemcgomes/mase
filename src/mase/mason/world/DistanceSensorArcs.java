/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
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
    private Class<? extends WorldObject>[] types = new Class[]{WorldObject.class};
    private Collection<? extends WorldObject> objects;
    private boolean binary = false;
    private WorldObject[] closestObjects;
    private double[] lastDistances;

    private boolean centerToCenter = false;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double orientationNoise = 0;
    private double rangeNoise = 0;
    private int noiseType;

    public DistanceSensorArcs(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    public void setArcs(double[] arcStart, double[] arcEnd) {
        if (arcStart.length != arcEnd.length) {
            throw new RuntimeException("Number of arc starts does not match arc ends. Starts: " + arcStart.length + " Ends: " + arcEnd.length);
        }
        this.arcStart = arcStart;
        this.arcEnd = arcEnd;
        this.closestObjects = new WorldObject[valueCount()];
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

    /**
     * Setting this makes the sensor ignore the object types, and use these
     * objects instead. Set null to ignore
     *
     * @param obj
     */
    public void setObjects(Collection<? extends WorldObject> obj) {
        this.objects = obj;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public void centerToCenter(boolean centerToCenter) {
        this.centerToCenter = centerToCenter;
    }

    @Override
    public int valueCount() {
        return arcStart.length;
    }

    /**
     * Very efficient implementation using an ordered TreeMap
     * Should ensure scalability when large numbers of objects are present,
     * as there is no need to check angles with objects that are farther than
     * the closest object in the given cone.
     * Potential limitation (unlikely): if there are two objects at exactly the same distance
     * but at different angles, only one of them will be considered, as the distance
     * is used as key in the TreeMap
     */    
    @Override
    public double[] readValues() {
        lastDistances = new double[valueCount()];
        Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(closestObjects, null);
        if (range < 0.001) {
            return lastDistances;
        }
        double rangeNoiseAbs = Double.isInfinite(range) ? rangeNoise * fieldDiagonal : range * rangeNoise;

        Collection<? extends WorldObject> candidates = getCandidates();

        TreeMap<Double,WorldObject> distances = new TreeMap<>();
        for (WorldObject o : candidates) {
            if(o != ag) {
                if(!centerToCenter && o.isInside(ag.getLocation())) {
                    Arrays.fill(lastDistances, 0);
                    Arrays.fill(closestObjects, o);
                    return lastDistances;                    
                }
                
                double dist = centerToCenter ? ag.getLocation().distance(o.getLocation()) : Math.max(0, ag.distanceTo(o));
                if (rangeNoiseAbs > 0) {
                    dist += rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                    dist = Math.max(dist, 0);
                }
                if(dist <= range) {
                    distances.put(dist ,o);                
                }                
            }
        }

        int filled = 0;
        for (Entry<Double,WorldObject> e : distances.entrySet()) {
            if (filled == arcStart.length) {
                break;
            }
            double angle = ag.angleTo(field.getObjectLocation(e.getValue()));
            if (orientationNoise > 0) {
                angle += orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                angle = EmboddiedAgent.normalizeAngle(angle);
            }
            for (int a = 0; a < arcStart.length; a++) {
                if (Double.isInfinite(lastDistances[a]) && ((angle >= arcStart[a] && angle <= arcEnd[a])
                        || (arcStart[a] > arcEnd[a] && (angle >= arcStart[a] || angle <= arcEnd[a])))) {
                    filled++;
                    lastDistances[a] = e.getKey();
                    closestObjects[a] = e.getValue();
                }
            }
        }
        return lastDistances;
    }

    protected Collection<? extends WorldObject> getCandidates() {
        if (objects != null) {
            return new ArrayList<>(objects);
        } else {
            Collection<WorldObject> objs = new LinkedList<>();
            Bag neighbours = (Double.isInfinite(range) || field.allObjects.size() < 30) ? field.allObjects
                    : field.getNeighborsWithinDistance(ag.getLocation(), range + ag.getRadius(), false, true);
            for (Object n : neighbours) {
                if (n != ag) {
                    for (Class type : types) {
                        if (type.isInstance(n)) {
                            objs.add((WorldObject) n);
                            break;
                        }
                    }
                }
            }
            return objs;
        }
    }

    public WorldObject[] getClosestObjects() {
        return closestObjects;
    }

    public double[] getLastDistances() {
        return lastDistances;
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
