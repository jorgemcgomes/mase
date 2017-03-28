/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import mase.mason.world.GeomUtils.Polygon;
import org.apache.commons.lang3.ArrayUtils;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ClosedPolygonObject extends MultilineObject {

    private static final long serialVersionUID = 1L;

    /**
     * @param boundary Should not repeat the first and last. I.e, number of edges = number of points
     */
    public ClosedPolygonObject(Continuous2D field, Double2D... boundary) {
        super(field, closeLoop(boundary));
    }
    
    // TODO: constructor with polygon -- must check that it is a closed polygon
    
    private static Double2D[] closeLoop(Double2D[] points) {
        // Already closed
        if(points[0].equals(points[points.length - 1])) {
            return points;
        }
        // Close loop: make segment with first--last
        Double2D[] closed = ArrayUtils.add(points, points[0]);
        return closed;
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
