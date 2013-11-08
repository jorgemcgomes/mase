/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.indiana;

import mase.app.indiana.Indiana.AgentPlacement;

/**
 *
 * @author jorge
 */
public class IndianaParams {
    
        public static final String P_SIZE = "size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_NUM_AGENTS = "num-agents";
    public static final String P_AGENT_RADIUS = "agent-sensor-radius";
    public static final String P_AGENT_ARCS = "agent-sensor-arcs";
    public static final String P_WALL_RADIUS = "wall-radius";
    public static final String P_WALL_RAYS = "wall-rays";
    public static final String P_AGENT_SPEED = "agent-speed";
    public static final String P_AGENT_ROTATION = "agent-rotation";
    public static final String P_GATE_SIZE = "gate-size";
    public static final String P_GATE_INTERVAL = "gate-interval";
    public static final String P_AGENT_PLACEMENT = "agent-placement";

    protected double size;
    protected double discretization;
    protected int numAgents;
    protected double agentSpeed;
    protected double agentSensorRadius;
    protected int agentSensorArcs;
    protected double wallRadius;
    protected int wallRays;
    protected double agentRotation;
    protected AgentPlacement agentPlacement;
    protected double gateSize;
    protected int gateInterval;
    
}
