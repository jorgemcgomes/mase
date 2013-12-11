/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.predcomp;

/**
 *
 * @author jorge
 */
public class PredcompParams {
    
    public static final String P_SIZE = "size";
    public static final String P_PROXIMITY_SENSORS = "proximity-sensors";
    public static final String P_PROXIMITY_RANGE = "proximity-range";
    public static final String P_VIEW_ANGLE = "view-angle";
    public static final String P_VISION_NEURONS = "vision-neurons";
    public static final String P_VISION_RANGE = "vision-range";
    public static final String P_PREY_SPEED = "prey-speed";
    public static final String P_PREDATOR_SPEED = "predator-speed";
    public static final String P_DISCRETIZATION = "discretization";
    
    protected double size;
    protected int proximitySensors;
    protected double proximityRange;
    protected double viewAngle;
    protected int visionNeurons;
    protected double visionRange;
    protected double preySpeed;
    protected double predatorSpeed;
    protected double discretization;
    
}
