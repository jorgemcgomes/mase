/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.LinkedHashSet;
import mase.generic.systematic.Entity;
import net.jafama.FastMath;
import sim.portrayal.simple.ShapePortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class StaticPolygon extends ShapePortrayal2D implements Entity {

    protected final Segment[] segments;
    protected final Double2D[] points;
    private static final double[] EMPTY_ARRAY = new double[]{};

    /**
     * Builds a polygon with all points connected
     *
     * @param base
     * @param points
     */
    public StaticPolygon(Double2D... points) {
        super(buildShape(points));

        this.segments = new Segment[points.length - 1];
        for (int i = 0; i < points.length - 1; i++) {
            segments[i] = new Segment(points[i], points[i + 1]);
        }
        this.points = points;
    }

    /**
     * Builds a polygon with the given segments
     *
     * @param base
     * @param segs
     */
    public StaticPolygon(Segment... segs) {
        super(buildShape(segs));

        LinkedHashSet<Double2D> ps = new LinkedHashSet<Double2D>();
        for (int i = 0; i < segs.length; i++) {
            ps.add(segs[i].start);
            ps.add(segs[i].end);
        }
        this.segments = segs;
        this.points = new Double2D[ps.size()];
        ps.toArray(points);
    }

    public double closestDistance(Double2D rayStart, Double2D rayEnd) {
        double closestDist = Double.POSITIVE_INFINITY;
        for (Segment seg : segments) {
            Double2D inters = segmentIntersection(rayStart, rayEnd, seg.start, seg.end);
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
            double d = distToSegment(testPoint, seg.start, seg.end);
            min = Math.min(min, Math.max(0, d));
        }
        return min;
    }

    public double closestDistance(StaticPolygon other) {
        double closest = Double.NEGATIVE_INFINITY;
        for (Double2D p : this.points) {
            closest = Math.min(closest, other.closestDistance(p));
        }
        for (Double2D p : other.points) {
            closest = Math.min(closest, this.closestDistance(p));
        }
        return closest;
    }

    private static Shape buildShape(Double2D[] points) {
        GeneralPath path = new GeneralPath();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        return path;
    }

    private static Shape buildShape(Segment[] segments) {
        GeneralPath path = new GeneralPath();
        for (Segment seg : segments) {
            path.moveTo(seg.start.x, seg.start.y);
            path.lineTo(seg.end.x, seg.end.y);
        }
        return path;
    }

    // Return minimum distance between line segment vw and point p
    public static double distToSegment(Double2D p, Double2D v, Double2D w) {

        double l2 = FastMath.pow2(v.x - w.x) + FastMath.pow2(v.y - w.y);
        if (l2 == 0.0) {
            return p.distance(v);   // v == w case
        }
        // Consider the line extending the segment, parameterized as v + t (w - v).
        // We find projection of point p onto the line. 
        // It falls where t = [(p-v) . (w-v)] / |w-v|^2
        double t = p.subtract(v).dot(w.subtract(v)) / l2;
        if (t < 0.0) {
            return p.distance(v); // Beyond the 'v' end of the segment
        } else if (t > 1.0) {
            return p.distance(w);  // Beyond the 'w' end of the segment
        }
        Double2D projection = v.add((w.subtract(v)).multiply(t));  // Projection falls on the segment
        return p.distance(projection);
    }

    // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    public static Double2D segmentIntersection(Double2D p0, Double2D p1, Double2D p2, Double2D p3) {
        Double2D s1 = new Double2D(p1.x - p0.x, p1.y - p0.y);
        Double2D s2 = new Double2D(p3.x - p2.x, p3.y - p2.y);
        double s = (-s1.y * (p0.x - p2.x) + s1.x * (p0.y - p2.y)) / (-s2.x * s1.y + s1.x * s2.y);
        double t = (s2.x * (p0.y - p2.y) - s2.y * (p0.x - p2.x)) / (-s2.x * s1.y + s1.x * s2.y);
        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // collision detected
            return new Double2D(p0.x + t * s1.x, p0.y + t * s1.y);
        } else {
            // no collision
            return null;
        }
    }

    @Override
    public double[] getStateVariables() {
        return EMPTY_ARRAY;
    }

    public static class Segment {

        public final Double2D start;
        public final Double2D end;

        public Segment(Double2D start, Double2D end) {
            this.start = start;
            this.end = end;
        }
        
        public Segment(double startX, double startY, double endX, double endY) {
            this.start = new Double2D(startX, startY);
            this.end = new Double2D(endX, endY);
        }

    }
}