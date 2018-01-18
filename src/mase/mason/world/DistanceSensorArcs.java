/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.MathUtils;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class DistanceSensorArcs extends AbstractSensor {

    private double[] arcStart;
    private double[] arcEnd;
    private double range = Double.POSITIVE_INFINITY;
    private Class<? extends WorldObject>[] types = new Class[]{WorldObject.class};
    private WorldObject[] objects;
    private boolean binary = false;
    private WorldObject[] closestObjects;
    private double[] lastDistances;

    private boolean centerToCenter = false;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double orientationNoise = 0;
    private double rangeNoise = 0;
    private int noiseType;
    
    private Color drawColor = Color.YELLOW;

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
        this.lastDistances = new double[valueCount()];
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
    
    public void setDrawColor(Color c) {
        this.drawColor = c;
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
        this.objects = obj.toArray(new WorldObject[obj.size()]);
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
     * Very efficient implementation using an ordered TreeMap Should ensure
     * scalability when large numbers of objects are present, as there is no
     * need to check angles with objects that are farther than the closest
     * object in the given cone. Potential limitation (unlikely): if there are
     * two objects at exactly the same distance but at different angles, only
     * one of them will be considered, as the distance is used as key in the
     * TreeMap
     */
    @Override
    public double[] readValues() {
        Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(closestObjects, null);
        if (range < 0.001) {
            return lastDistances;
        }
        double rangeNoiseAbs = Double.isInfinite(range) ? rangeNoise * fieldDiagonal : range * rangeNoise;

        WorldObject[] candidates = getCandidates();

        // TODO: replace treemap with collection-sort
        Pair<Double, WorldObject>[] distances = new Pair[candidates.length];
        int index = 0;
        for (WorldObject o : candidates) {
            if (!centerToCenter && o.isInside(ag.getLocation())) {
                Arrays.fill(lastDistances, 0);
                Arrays.fill(closestObjects, o);
                return lastDistances;
            }

            double dist = centerToCenter ? ag.getLocation().distance(o.getLocation()) : Math.max(0, ag.distanceTo(o));
            if (rangeNoiseAbs > 0) {
                dist += rangeNoiseAbs * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                dist = Math.max(dist, 0);
            }
            if (dist <= range) {
                distances[index++] = Pair.of(dist, o);
            }
        }
        if (index < distances.length) {
            distances = Arrays.copyOf(distances, index);
        }

        Arrays.sort(distances, new Comparator<Pair<Double, WorldObject>>() {
            @Override
            public int compare(Pair<Double, WorldObject> a, Pair<Double, WorldObject> b) {
                return Double.compare(a.getLeft(), b.getLeft());
            }
        });

        int filled = 0;
        for (Pair<Double, WorldObject> e : distances) {
            if (filled == arcStart.length) {
                break;
            }
            double angle = ag.angleTo(e.getRight().getLocation());
            if (orientationNoise > 0) {
                angle += orientationNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
                angle = MathUtils.normalizeAngle(angle, 0);
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

    protected WorldObject[] getCandidates() {
        if (objects != null) {
            return objects;
        } else {
            Bag neighbours = (Double.isInfinite(range) || field.allObjects.size() < 30) ? field.allObjects
                    : field.getNeighborsWithinDistance(ag.getLocation(), range + ag.getRadius(), false, true);
            WorldObject[] objs = new WorldObject[neighbours.size()];
            int index = 0;
            for (Object n : neighbours) {
                if (n != ag) {
                    for (Class type : types) {
                        if (type.isInstance(n)) {
                            objs[index++] = (WorldObject) n;
                            break;
                        }
                    }
                }
            }
            if (index < objs.length) {
                objs = Arrays.copyOf(objs, index);
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

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Rectangle2D.Double draw = info.draw;
        Double2D baseStart = new Double2D(centerToCenter ? 0.001 : ag.getRadius(), 0);
        Double2D baseEnd = new Double2D((centerToCenter ? 0 : ag.getRadius()) + (Double.isInfinite(range) ? fieldDiagonal : range), 0);
        for (int i = 0; i < arcStart.length; i++) {
            Double2D leftRayStart = baseStart.rotate(ag.orientation2D() + arcStart[i]);
            Double2D leftRayEnd = baseEnd.rotate(ag.orientation2D() + arcStart[i]);
            Double2D rightRayStart = baseStart.rotate(ag.orientation2D() + arcEnd[i]);
            Double2D rightRayEnd = baseEnd.rotate(ag.orientation2D() + arcEnd[i]);
            drawRay(leftRayStart, leftRayEnd, graphics, draw);
            drawRay(rightRayStart, rightRayEnd, graphics, draw);
            if (!Double.isInfinite(lastDistances[i])) {
                double d = centerToCenter ? lastDistances[i] : lastDistances[i] + ag.getRadius();
                Arc2D arc = new Arc2D.Double(draw.x - d * draw.width, draw.y - d * draw.height,
                        d * 2 * draw.width, d * 2 * draw.height,
                        Math.toDegrees((Math.PI * 2 - MathUtils.normalizeAngle(ag.orientation2D() + (arcEnd[i] > arcStart[i] ? arcEnd[i] : arcStart[i]), Math.PI)) ),
                        Math.toDegrees(arcEnd[i] > arcStart[i] ? arcEnd[i] - arcStart[i] : Math.PI - arcStart[i] + arcEnd[i]), Arc2D.PIE);
                graphics.setPaint(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 30));
                graphics.fill(arc);
            }
        }
    }

    private void drawRay(Double2D rs, Double2D re, Graphics2D graphics, Rectangle2D.Double draw) {
        int x1 = (int) (rs.x * draw.width + draw.x);
        int y1 = (int) (rs.y * draw.height + draw.y);
        int x2 = (int) (re.x * draw.width + draw.x);
        int y2 = (int) (re.y * draw.height + draw.y);
        graphics.setPaint(drawColor);
        graphics.setStroke(new BasicStroke(1));
        graphics.drawLine(x1, y1, x2, y2);
    }

}
