/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Fixed2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CircularObject extends CircledPortrayal2D implements Fixed2D, SensableObject {

    public static final Color DEFAULT_COLOR = Color.RED;
    
    private static final long serialVersionUID = 1L;
    protected Continuous2D field;
    protected Double2D pos;
    protected final double radius;
    protected SimState sim;
    private final int hash;
    private String label;

    protected CircledPortrayal2D circledPortrayal;
    protected LabelledPortrayal2D labelPortrayal;
    protected MovablePortrayal2D movablePortrayal;
    protected SimplePortrayal2D woChild;


    public CircularObject(SimState sim, Continuous2D field, double radius) {
        this(DEFAULT_COLOR, sim, field, radius);
    }
    
    public CircularObject(Color color, SimState sim, Continuous2D field, double radius) {
        this(new OvalPortrayal2D(color, radius * 2, true), sim, field, radius);
    }    
    
    public CircularObject(SimplePortrayal2D child, SimState sim, Continuous2D field, double radius) {
        super(new LabelledPortrayal2D(new MovablePortrayal2D(child), ""));

        this.woChild = child;
        this.sim = sim;
        this.field = field;
        this.radius = radius;
        
        this.circledPortrayal = this;
        this.circledPortrayal.setOnlyCircleWhenSelected(true);
        this.circledPortrayal.scale = radius * 2 + field.width / 50;

        this.hash = System.identityHashCode(this);
        this.label = this.toString();
        this.labelPortrayal = (LabelledPortrayal2D) circledPortrayal.child;
        this.labelPortrayal.label = label;
        //this.labelPortrayal.align = LabelledPortrayal2D.ALIGN_CENTER;

        this.movablePortrayal = (MovablePortrayal2D) labelPortrayal.child;
        movablePortrayal.setSelectsWhenMoved(true);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    public double getRadius() {
        return radius;
    }

    public void setColor(Color c) {
        this.circledPortrayal.paint = c;
        this.labelPortrayal.paint = Color.BLACK;
    }

    
    public void setLabel(String lab) {
        this.label = lab;
        labelPortrayal.label = lab;
    }

    @Override
    public String toString() {
        return label != null ? label : super.toString();
    }
    
    @Override
    public boolean maySetLocation(Object field, Object newObjectLocation) {
        this.setLocation((Double2D) newObjectLocation);
        return true;
    }

    public void setLocation(Double2D loc) {
        this.pos = loc;
        field.setObjectLocation(this, pos);
    }

    
    
    /*public double distanceTo(CircularObject other) {
        return Math.max(0, pos.distance(other.getLocation()) - other.radius - this.radius);
    }

    public double distanceTo(Double2D point) {
        return Math.max(0, pos.distance(point) - this.radius);
    }
    
    public double distanceTo(StaticPolygonObject poly) {
        return Math.max(0, poly.closestDistance(pos) - this.radius);
    }
    
    public double distanceTo(Object obj) {
         if(obj instanceof CircularObject) {
            return distanceTo((CircularObject) obj);
        } else if(obj instanceof Double2D) {
            return distanceTo((Double2D) obj);
        } else if(obj instanceof StaticPolygonObject) {
            return distanceTo((StaticPolygonObject) obj);
        } else {
            Double2D p = field.getObjectLocation(obj);
            if(p == null) {
                return Double.NaN;
            }
            return Math.max(0, p.distance(pos) - this.radius);
        }
    }*/

    /*public double centerDistanceTo(Object obj) {
        Double2D center;
        if(obj instanceof CircularObject) {
            center = ((CircularObject) obj).getLocation();
        } else if(obj instanceof Double2D) {
            center = (Double2D) obj;
        } else {
            center = field.getObjectLocation(obj);
        }        
        return center == null ? Double.NaN : pos.distance(center);
    }    */

    @Override
    public Double2D getCenterLocation() {
        return pos;
    }    
    
    @Override
    public double distanceTo(EmboddiedAgent ag) {
        return Math.max(0, pos.distance(ag.getCenterLocation()) - ag.getRadius() - getRadius());
    }

    @Override
    public boolean isInside(EmboddiedAgent ag) {
        return pos.distance(ag.getCenterLocation()) < ag.getRadius();
    }

    @Override
    public double closestRayIntersection(Double2D start, Double2D end) {
        throw new UnsupportedOperationException("Not supported yet: ray-circle intersection");
    }
}
