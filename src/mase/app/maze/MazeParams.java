/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.mason.ParamUtils.Param;
import mase.mason.world.GeomUtils.Polygon;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeParams {

    protected Polygon maze;
    protected Double2D startPos;
    protected Double2D targetPos;

    @Param(name = "maze")
    public String mazeFile;
    @Param(name = "linear-speed")
    public double linearSpeed;
    @Param(name = "turn-speed")
    public double turnSpeed;
    @Param(name = "agent-radius")
    public double agentRadius;
    @Param(name = "start-orientation")
    public double startOrientation;
    @Param(name = "target-radius")
    public double targetRadius;
    @Param(name = "sensor-range")
    public double sensorRange;

}
