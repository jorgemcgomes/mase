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
    
    int arenaSize;
    
    int minObjects;
    int maxObjects;
    double objectRadius;
    double minObjectDistance;
    double objectSpeed;
    
    int minObstacles;
    int maxObstacles;
    double minObstacleSize;
    double maxObstacleSize;
    
    boolean randomPosition;
    double radius;
    double linearSpeed;
    double linearAcc;
    boolean backMove;
    double turnSpeed;
    double turnAcc;
    int numCones;
    double coneRange;
    double whiskerRange;
    boolean differentialDrive;
    
}
