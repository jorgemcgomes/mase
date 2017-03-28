/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import mase.mason.generic.systematic.Entity;
import mase.mason.world.GeomUtils.Polygon;
import mase.mason.world.GeomUtils.Segment;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.ShapePortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MultilineObject extends ShapePortrayal2D implements Entity, WorldObject {
    private static final long serialVersionUID = 1L;

    protected final Continuous2D field;
    private final Polygon defPolygon;
    protected Polygon poly;
    private static final double[] EMPTY_ARRAY = new double[]{};

    public MultilineObject(Continuous2D field, Double2D... points) {
        this(field, new Polygon(points));
    }
    
    public MultilineObject(Continuous2D field, Segment... segments) {
        this(field, new Polygon(segments));
    }
    
    public MultilineObject(Continuous2D field, Polygon pol) {
        super(pol.buildShape());
        this.field = field;
        this.defPolygon = pol;
        this.poly = pol;
    }
   
    public Polygon getPolygon() {
        return poly;
    }
    
    
    public void setLocation(Double2D loc) {
        this.poly = defPolygon.add(loc);
        field.setObjectLocation(this, loc);
    }
    
    /*
    If the given point is potentially within the distance, it returns true for sure.
    Otherwise, its just best-effort. Might be wrong.
    */
    public boolean quickProximityCheck(Double2D point, double distance) {
        return point.x > poly.boundingBox.getLeft().x - distance &&
                point.y > poly.boundingBox.getLeft().y - distance &&
                point.x < poly.boundingBox.getRight().x + distance &&
                point.y < poly.boundingBox.getRight().y + distance;
    }


    @Override
    public double[] getStateVariables() {
        return EMPTY_ARRAY;
    }



    @Override
    public Double2D getLocation() {
        return poly.center;
    }


    @Override
    public boolean isInside(Double2D p) {
        return false;
    }

    @Override
    public double closestRayIntersection(Double2D start, Double2D end) {
        return poly.closestDistance(start, end);
    }

    @Override
    public double distanceTo(Double2D p) {
        return poly.closestDistance(p);
    }


}
