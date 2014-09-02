/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

/**
 *
 * @author jorge
 */
public class MRParams {
    
    protected double size;
    protected double discretization;
    protected int numAgents;
    protected double separation;
    protected double speed;
    protected double rotationSpeed;
    protected int sensorArcs;
    protected int numRocks;
    protected double rockRadius;
    protected double sensorRange;
    protected int minActivationTime;
    
    public static final String P_SIZE = "size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_NUM_AGENTS = "num-agents";
    public static final String P_SEPARATION = "separation";
    public static final String P_SPEED = "speed";
    public static final String P_ROTATION_SPEED = "rotation-speed";
    public static final String P_SENSOR_ARCS = "sensor-arcs";
    public static final String P_SENSOR_RANGE = "sensor-range";
    public static final String P_NUM_ROCKS = "num-rocks";
    public static final String P_ROCK_RADIUS = "rock-radius";
    public static final String P_MIN_ACTIVATION_TIME = "min-activation-time";
    
}
