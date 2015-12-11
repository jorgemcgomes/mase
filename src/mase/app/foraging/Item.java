/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Item extends OvalPortrayal2D {

    private static final long serialVersionUID = 1L;

    protected final double radius;
    protected final Double2D position;

    public Item(double radius, Double2D position) {
        super(new Color(0,153,0), radius * 2, true);
        this.radius = radius;
        this.position = position;
    }

    public Double2D getLocation() {
        return position;
    }
}
