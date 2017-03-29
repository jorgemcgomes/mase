/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import mase.mason.world.GeomUtils.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PolygonObject extends MultilineObject {

    private static final long serialVersionUID = 1L;

    /**
     * @param boundary Should not repeat the first and last. I.e, number of edges = number of points
     */
    public PolygonObject(Continuous2D field, Double2D... boundary) {
        super(field, closeLoop(boundary));
    }
    
    public PolygonObject(Continuous2D field, Multiline poly) {
        super(field, checkClosed(poly));
    }
    
    // TODO: constructor with polygon -- must check that it is a closed polygon
    
    private static Double2D[] closeLoop(Double2D[] points) {
        if(points.length < 3) {
            throw new RuntimeException("Not enough points for a closed polygon: " + points.length);
        }
        // Already closed
        if(points[0].equals(points[points.length - 1])) {
            return points;
        }
        // Close loop: make segment with first--last
        Double2D[] closed = ArrayUtils.add(points, points[0]);
        return closed;
    }
    
    private static Multiline checkClosed(Multiline poly) {
        if(poly.segments.length < 3) {
            throw new RuntimeException("Not enough segments: " + poly.segments.length);
        }        
        for(int i = 0 ; i < poly.segments.length ; i++) {
            if((i == 0 && !poly.segments[i].start.equals(poly.segments[poly.segments.length - 1].end)) 
                    || !poly.segments[i].start.equals(poly.segments[i-1].end)) {
                throw new RuntimeException("Provided polygon is not closed: " + poly);
            }
        }
        return poly;
    }

    @Override
    public boolean isInside(Double2D p) {
        // outside the bounding box
        if(!poly.isInsideBB(p)) {
            return false;
        }
        return GeomUtils.pointInPolygon(p, poly.points);
    }

}
