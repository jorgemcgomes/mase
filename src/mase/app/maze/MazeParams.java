/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.mason.world.StaticPolygon;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeParams {

    public static final String P_MAZE = "maze";
    public static final String P_LINEAR_SPEED = "linear-speed";
    public static final String P_TURN_SPEED = "turn-speed";
    public static final String P_AGENT_RADIUS = "agent-radius";
    public static final String P_SENSOR_RANGE = "sensor-range";
    public static final String P_START_POS = "start-pos";
    public static final String P_START_ORI = "start-orientation";
    public static final String P_TARGET_POS = "target-pos";
    public static final String P_TARGET_RADIUS = "target-radius";

    protected StaticPolygon maze;
    protected double linearSpeed;
    protected double turnSpeed;
    protected double agentRadius;
    protected Double2D startPos;
    protected double startOrientation;
    protected Double2D targetPos;
    protected double targetRadius;
    protected double sensorRange;

}
