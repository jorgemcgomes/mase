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
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class WorldObject extends CircledPortrayal2D implements Fixed2D {

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
    protected SimplePortrayal2D child;

    public WorldObject(SimplePortrayal2D child, SimState sim, Continuous2D field, double radius) {
        super(new LabelledPortrayal2D(new MovablePortrayal2D(child), ""));

        this.child = child;
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
        this.labelPortrayal.paint = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
    }

    public Double2D getLocation() {
        return pos;
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

}
