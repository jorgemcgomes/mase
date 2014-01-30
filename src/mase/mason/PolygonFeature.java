/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import net.jafama.FastMath;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PolygonFeature implements EnvironmentalFeature {

    private final Double2D[] segStarts, segEnds;

    public PolygonFeature(Double2D[] segStarts, Double2D[] segEnds) {
        this.segStarts = segStarts;
        this.segEnds = segEnds;
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
    public double distanceTo(Double2D position) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < segStarts.length; i++) {
            double d = distToSegment(position, segStarts[i], segEnds[i]);
            min = Math.min(min, Math.max(0, d));
        }
        return min;
    }
    
    public Double2D[] getSegStarts() {
        return segStarts;
    }

    public Double2D[] getSegEnds() {
        return segEnds;
    }
    
    
}
