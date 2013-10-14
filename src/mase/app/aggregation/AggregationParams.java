/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

/**
 *
 * @author jorge
 */
public class AggregationParams {
    
    public static final String P_SIZE = "size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_NUM_AGENTS = "num-agents";
    public static final String P_AGENT_RADIUS = "agent-radius";
    public static final String P_AGENT_ARCS = "agent-arcs";
    public static final String P_WALL_RADIUS = "wall-radius";
    public static final String P_WALL_RAYS = "wall-rays";
    public static final String P_AGENT_SPEED = "agent-speed";
    public static final String P_AGENT_ROTATION = "agent-rotation";
    
    
    protected double size;
    protected double discretization;
    protected int numAgents;
    protected double agentSpeed;
    protected double agentRadius;
    protected int agentArcs;
    protected double wallRadius;
    protected int wallRays;
    protected double agentRotation;
    
}
