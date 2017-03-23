/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.util.LinkedHashSet;
import mase.mason.generic.systematic.Entity;
import mase.mason.world.PolygonUtils.Segment;
import org.apache.commons.lang3.tuple.Pair;
import sim.portrayal.simple.ShapePortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class StaticMultilineObject extends ShapePortrayal2D implements Entity, SensableObject {
    private static final long serialVersionUID = 1L;

    private final Segment[] segments;
    private final Double2D[] points;
    private final Pair<Double2D, Double2D> boundingBox;
    private final Double2D center;
    private final double width, height;
    private static final double[] EMPTY_ARRAY = new double[]{};

    /**
     * Builds a polygon with all points connected
     *
     * @param base
     * @param points
     */
    public StaticMultilineObject(Double2D... points) {
        super(PolygonUtils.buildShape(points));

        this.segments = new Segment[points.length - 1];
        for (int i = 0; i < points.length - 1; i++) {
            segments[i] = new Segment(points[i], points[i + 1]);
        }
        this.points = points;
        this.boundingBox = PolygonUtils.computeBB(points);
        this.width = boundingBox.getRight().x - boundingBox.getLeft().x;
        this.height = boundingBox.getRight().y - boundingBox.getLeft().y;
        this.center = new Double2D((boundingBox.getLeft().x + boundingBox.getRight().x) /2,
                (boundingBox.getLeft().y + boundingBox.getRight().y) /2);
    }

    /**
     * Builds a polygon with the given segments
     *
     * @param base
     * @param segs
     */
    public StaticMultilineObject(Segment... segs) {
        super(PolygonUtils.buildShape(segs));

        LinkedHashSet<Double2D> ps = new LinkedHashSet<>();
        for (int i = 0; i < segs.length; i++) {
            ps.add(segs[i].start);
            ps.add(segs[i].end);
        }
        this.segments = segs;
        this.points = new Double2D[ps.size()];
        ps.toArray(points);
        this.boundingBox = PolygonUtils.computeBB(points);
        this.width = boundingBox.getRight().x - boundingBox.getLeft().x;
        this.height = boundingBox.getRight().y - boundingBox.getLeft().y;
        this.center = new Double2D((boundingBox.getLeft().x + boundingBox.getRight().x) /2,
                (boundingBox.getLeft().y + boundingBox.getRight().y) /2);
    }

    public double closestDistance(Double2D rayStart, Double2D rayEnd) {
        double closestDist = Double.POSITIVE_INFINITY;
        for (Segment seg : segments) {
            Double2D inters = PolygonUtils.segmentIntersection(rayStart, rayEnd, seg.start, seg.end);
            if (inters != null) {
                double d = rayStart.distance(inters);
                if (d < closestDist) {
                    closestDist = d;
                }
            }
        }
        return closestDist;
    }
    
    public double closestDistance(Double2D testPoint) {
        double min = Double.POSITIVE_INFINITY;
        for (Segment seg : segments) {
            double d = PolygonUtils.distToSegment(testPoint, seg.start, seg.end);
            min = Math.min(min, Math.max(0, d));
        }
        return min;
    }
    
    public double closestDistance(StaticMultilineObject other) {
        double closest = Double.NEGATIVE_INFINITY;
        for (Double2D p : this.points) {
            closest = Math.min(closest, other.closestDistance(p));
        }
        for (Double2D p : other.points) {
            closest = Math.min(closest, this.closestDistance(p));
        }
        return closest;
    }

    public Pair<Double, Segment> closestSegment(Double2D testPoint) {
        double min = Double.POSITIVE_INFINITY;
        Segment s = null;
        for (Segment seg : segments) {
            double d = PolygonUtils.distToSegment(testPoint, seg.start, seg.end);
            if(d < min) {
                min = d;
                s = seg;
            }
        }
        return Pair.of(min, s);        
    }

    public Segment[] segments() {
        return segments;
    }    
    
    @Override
    public double[] getStateVariables() {
        return EMPTY_ARRAY;
    }

    public Pair<Double2D, Double2D> getBoundingBox() {
        return boundingBox;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }


    @Override
    public Double2D getCenterLocation() {
        return center;
    }

    @Override
    public double distanceTo(EmboddiedAgent ag) {
        return closestDistance(ag.getCenterLocation()) - ag.getRadius();
    }

    @Override
    public boolean isInside(EmboddiedAgent ag) {
        return false;
    }

    @Override
    public double closestRayIntersection(Double2D start, Double2D end) {
        return closestDistance(start, end);
    }

}
