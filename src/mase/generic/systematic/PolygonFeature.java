/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

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
    private double distToSegment(Double2D p, Double2D v, Double2D w) {

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

    @Override
    public double distanceTo(Double2D position) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < segStarts.length; i++) {
            double d = distToSegment(position, segStarts[i], segEnds[i]);
            min = Math.min(min, Math.max(0, d));
        }
        return min;
    }

    @Override
    public double[] getStateVariables() {
        return new double[0];
    }

    public Double2D[] getSegStarts() {
        return segStarts;
    }

    public Double2D[] getSegEnds() {
        return segEnds;
    }
    
    
}
