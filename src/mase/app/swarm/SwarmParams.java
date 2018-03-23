/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import mase.app.playground.*;
import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class SwarmParams implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public int arenaSize;
    
    public int minSwarmSize;
    public int maxSwarmSize;
    public int safetyDistance;
    
    public int minObjects;
    public int maxObjects;
    public double objectRadius;
    public double objectSpeed;
    
    public int minObstacles;
    public int maxObstacles;
    public double minObstacleSize;
    public double maxObstacleSize;
    
    public double radius;
    public double wheelSpeed;
    public double wheelAcc;
    public boolean backMove;
    public int numCones;
    public double coneRange;
    public double whiskerRange;
    public int poiNumCones;
    public double poiConeRange;
    public boolean differentialDrive;
    
}
