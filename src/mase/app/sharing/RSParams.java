/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

/**
 *
 * @author jorge
 */
public class RSParams {

    public static final String P_SIZE = "size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_NUM_AGENTS = "num-agents";
    public static final String P_AGENT_SENSOR_RANGE = "agent-sensor-range";
    public static final String P_AGENT_SENSOR_ARCS = "agent-sensor-arcs";
    //public static final String P_WALL_RADIUS = "wall-radius";
    //public static final String P_WALL_RAYS = "wall-rays";
    public static final String P_AGENT_SPEED = "agent-speed";
    public static final String P_AGENT_ROTATION = "agent-rotation";
    public static final String P_AGENT_RADIUS = "agent-radius";
    public static final String P_RESOURCE_RADIUS = "resource-radius";
    public static final String P_MAX_ENERGY = "max-energy";
    public static final String P_MIN_ENERGY_DECAY = "min-energy-decay";
    public static final String P_MAX_ENERGY_DECAY = "max-energy-decay";
    public static final String P_RECHARGE_RATE = "recharge-rate";
    public static final String P_RECHARGE_DELAY = "recharge-delay";
    
    protected double size;
    protected double discretization;
    protected int numAgents;
    protected double agentSpeed;
    protected double agentRadius;
    protected double agentSensorRange;
    protected int agentSensorArcs;
    //protected double wallRadius;
    //protected int wallRays;
    protected double agentRotation;
    protected double resourceRadius;
    protected double maxEnergy;
    protected double minEnergyDecay;
    protected double maxEnergyDecay;
    protected double rechargeRate;
    protected int rechargeDelay;
}
