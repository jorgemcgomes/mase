/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Fixed2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PointObject extends OvalPortrayal2D implements Fixed2D, WorldObject {

    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final double DEFAULT_SIZE = 1;
    private static final long serialVersionUID = 1L;
    private Double2D point;
    private Continuous2D field;
    private final int hash;

    public PointObject(Color color, Continuous2D field, Double2D point) {
        super(color, DEFAULT_SIZE, false);
        this.point = point;
        this.field = field;
        this.hash = System.identityHashCode(this);
        field.setObjectLocation(this, point);
    }

    public PointObject(Continuous2D field, Double2D point) {
        this(DEFAULT_COLOR, field, point);
    }

    @Override
    public Double2D getLocation() {
        return point;
    }

    public void setLocation(Double2D p) {
        this.point = p;
        this.field.setObjectLocation(this, point);
    }

    @Override
    public double closestRayIntersection(Double2D start, Double2D end) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean isInside(Double2D p) {
        return false;
    }

    @Override
    public double distanceTo(Double2D p) {
        return point.distance(p);
    }

    @Override
    public boolean maySetLocation(Object field, Object newObjectLocation) {
        this.setLocation((Double2D) newObjectLocation);
        return true;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }    
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
    
    @Override
    public String toString() {
        return point.toString();
    }
}
