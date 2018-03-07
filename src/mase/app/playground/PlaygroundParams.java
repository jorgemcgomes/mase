/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class PlaygroundParams implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public int arenaSize;
    
    public int minObjects;
    public int maxObjects;
    public double objectRadius;
    public double minObjectDistance;
    public double objectSpeed;
    
    public int minObstacles;
    public int maxObstacles;
    public double minObstacleSize;
    public double maxObstacleSize;
    
    public boolean randomPosition;
    public boolean randomOrientation;
    public double radius;
    public double linearSpeed;
    public double linearAcc;
    public boolean backMove;
    public double turnSpeed;
    public double turnAcc;
    public int numCones;
    public double coneRange;
    public double whiskerRange;
    public boolean differentialDrive;
    
}
