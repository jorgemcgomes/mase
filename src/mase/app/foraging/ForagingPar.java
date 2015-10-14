/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingPar implements Cloneable {
    
    public enum PlacementMode {
        fixed, semirandom, random;
    }
    
    public double flyingRadius;
    public double flyingVisionAngle;
    public int flyingArcs;
    public double landVisionAngle;
    public double landSensingRange;
    public int landArcs;

    public double landRadius;
    public double landLinearSpeed;
    public double landTurnSpeed;
    public double flyingLinearSpeed;
    public double flyingLinearAcc;
    public double flyingAngSpeed;
    public double flyingAngAcc;
    public double flyingMaxHeight;
    public boolean useFlyingRobot;

    public Double2D arenaSize;
    public Double2D[] items;
    public double itemPlacementZone;
    public double itemRadius;
    
    public PlacementMode flyingPlacement;
    public PlacementMode landPlacement;
    public Double2D flyingStartPos;
    public Double2D landStartPos;
    public double flyingStartHeight;
    public double flyingStartOri;
    public double landStartOri;
            
    public boolean flyingVerticalMovement;

}
