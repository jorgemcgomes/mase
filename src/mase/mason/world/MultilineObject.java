/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import mase.mason.generic.systematic.Entity;
import mase.mason.world.GeomUtils.Multiline;
import mase.mason.world.GeomUtils.Segment;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Fixed2D;
import sim.portrayal.simple.ShapePortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MultilineObject extends ShapePortrayal2D implements Entity, WorldObject, Fixed2D {
    private static final long serialVersionUID = 1L;

    protected final Continuous2D field;
    private final Multiline defPolygon;
    protected Multiline poly;
    private static final double[] EMPTY_ARRAY = new double[]{};
    private final int hash;

    public MultilineObject(Continuous2D field, Double2D... points) {
        this(field, new Multiline(points));
    }
    
    public MultilineObject(Continuous2D field, Segment... segments) {
        this(field, new Multiline(segments));
    }
    
    public MultilineObject(Continuous2D field, Multiline pol) {
        super(pol.buildShape());
        this.field = field;
        this.defPolygon = pol;
        this.poly = pol;
        this.hash = System.identityHashCode(this);
    }
   
    public Multiline getPolygon() {
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

    @Override
    public boolean maySetLocation(Object field, Object newObjectLocation) {
        this.setLocation((Double2D) newObjectLocation);
        return true;    
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }    
    
    @Override
    public int hashCode() {
        return hash;
    }
}
