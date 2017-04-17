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
    
    int minObstacles;
    int maxObstacles;
    double minObstacleSize;
    double maxObstacleSize;
    
    double radius;
    double linearSpeed;
    double turnSpeed;
    int numCones;
    double coneRange;
    double whiskerRange;
    
}
