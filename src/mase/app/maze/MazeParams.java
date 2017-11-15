/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.io.Serializable;
import mase.mason.ParamUtils.IgnoreParam;
import mase.mason.world.GeomUtils.Multiline;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @IgnoreParam
    protected Multiline maze[];
    
    public Double2D startPos[] = null;
    public Double2D targetPos[] = null;
    public double startOrientation[] = null;
    public double xRandom = 0;
    public double yRandom = 0;

    public Double2D[] zonesLowerCorner;
    public Double2D[] zonesUpperCorner;
    public double[] zonesMaxSpeed;
    public int maxSpeedingSteps;
    public double maxSpeedingAccum;
    
    public boolean instaKill = false;
    
    public String mazeFile[];
    public double linearSpeed;
    public double turnSpeed;
    public double agentRadius;
    public double targetRadius;
    public double sensorRange;

}
