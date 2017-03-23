/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import mase.mason.world.CircularObject;

/**
 *
 * @author jorge
 */
public class Item extends CircularObject {

    private static final long serialVersionUID = 1L;

    public Item(ForagingTask sim, double radius) {
        super(new Color(0,153,0), sim, sim.field, radius);
    }

}
