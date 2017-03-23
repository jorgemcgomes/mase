/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class StaticPointObject extends OvalPortrayal2D implements SensableObject {
    
    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final double DEFAULT_SIZE = 1;
    private static final long serialVersionUID = 1L;
    private final Double2D point;
    
    public StaticPointObject(Color color, Continuous2D field, Double2D point) {
        super(color, DEFAULT_SIZE, false);
        this.point = point;
        if(field != null) {
            field.setObjectLocation(this, point);
        }
    }

    public StaticPointObject(Continuous2D field, Double2D point) {
        this(DEFAULT_COLOR, field, point);
    }

    @Override
    public Double2D getCenterLocation() {
        return point;
    }

    @Override
    public double closestRayIntersection(Double2D start, Double2D end) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double distanceTo(EmboddiedAgent ag) {
        return point.distance(ag.getCenterLocation()) - ag.getRadius();
    }

    @Override
    public boolean isInside(EmboddiedAgent ag) {
        return false;
    }
    
}
