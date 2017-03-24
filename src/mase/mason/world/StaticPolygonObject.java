/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import org.apache.commons.lang3.ArrayUtils;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class StaticPolygonObject extends StaticMultilineObject {

    private static final long serialVersionUID = 1L;
    private final Double2D[] boundary;

    /**
     * @param boundary Should not repeat the first and last. I.e, number of edges = number of points
     */
    public StaticPolygonObject(Double2D... boundary) {
        super(closeLoop(boundary));
        this.boundary = boundary;
    }
    
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
        if(!GeomUtils.isInsideBB(getBoundingBox(), p)) {
            return false;
        }
        return GeomUtils.pointInPolygon(p, boundary);
    }

}
