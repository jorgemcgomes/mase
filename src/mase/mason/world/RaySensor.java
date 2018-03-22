/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * @author jorge
 */
public class RaySensor extends AbstractSensor {

    private Double2D[] rayStarts, rayEnds;
    private boolean binary = false;
    private double range;
    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double rangeNoise = 0;
    private double orientationNoise = 0;
    private int noiseType;
    private double[] angles;
    private WorldObject[] objects;
    private double[] lastDistances;
    private Class<? extends WorldObject>[] types = new Class[]{WorldObject.class};
    private Color drawColor = Color.BLACK;

    public RaySensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }
    
    public void setDrawColor(Color c) {
        this.drawColor = c;
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
    public void setObjects(Collection<? extends WorldObject> objects) {
        this.objects = objects.toArray(new WorldObject[objects.size()]);
    }

    public void setObjectTypes(Class... types) {
        this.types = types;
    }

    public void setRays(double range, double... angles) {
        this.range = range;
        this.angles = angles;
        if (Double.isInfinite(range)) {
            range = fieldDiagonal;
        }

        rayStarts = new Double2D[angles.length];
        rayEnds = new Double2D[angles.length];
        lastDistances = new double[angles.length];

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
    public double[] readValues() {
        Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
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

            for (WorldObject p : getCandidates()) {
                double dist = p.closestRayIntersection(rs, re);
                lastDistances[i] = Math.min(lastDistances[i], dist);
            }
        }
        return lastDistances;
    }

    protected WorldObject[] getCandidates() {
        if (objects != null) {
            return objects;
        } else {
            // TODO: due to polygon location in the field limitations, you cannot rely on nearest neighbours
            Bag neighbours = field.allObjects;
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

    @Override
    public double[] normaliseValues(double[] vals) {
        double[] norm = new double[vals.length];
        double max = Double.isInfinite(range) ? fieldDiagonal : range;
        for (int i = 0; i < vals.length; i++) {
            if (binary) {
                norm[i] = Double.isInfinite(vals[i]) ? -1 : 1;
            } else {
                norm[i] = Double.isInfinite(vals[i]) ? 1 : (vals[i] / max) * 2 - 1;
            }
        }
        return norm;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        Rectangle2D.Double draw = info.draw;
        for (int i = 0; i < rayStarts.length; i++) {
            Double2D rs = rayStarts[i].rotate(ag.orientation2D());
            Double2D re = rayEnds[i].rotate(ag.orientation2D());
            int x1 = (int) (rs.x * draw.width + draw.x);
            int y1 = (int) (rs.y * draw.height + draw.y);
            int x2 = (int) (re.x * draw.width + draw.x);
            int y2 = (int) (re.y * draw.height + draw.y);
            graphics.setPaint(drawColor);
            graphics.setStroke(new BasicStroke(1));
            graphics.drawLine(x1, y1, x2, y2);
            if (!Double.isInfinite(lastDistances[i])) {
                Double2D hit = GeomUtils.pointInLine(rs, re, lastDistances[i]);
                graphics.setPaint(Color.RED);
                graphics.fillOval((int) (hit.x * draw.width + draw.x) - 3, (int) (hit.y * draw.height + draw.y) - 3, 6, 6);
            }
        }
    }

}
