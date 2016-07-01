/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class RockType implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final Color color;
    protected final int[] actuators;
    protected final int collectionTime;
    protected final double radius;

    public RockType(Color color, int[] actuators, int collectionTime, double radius) {
        this.color = color;
        this.actuators = actuators;
        this.collectionTime = collectionTime;
        this.radius = radius;
    }

}
