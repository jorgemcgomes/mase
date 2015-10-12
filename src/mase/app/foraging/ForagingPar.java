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
    
    protected double flyingRadius;
    protected double flyingVisionAngle;
    protected int flyingArcs;
    protected double landVisionAngle;
    protected double landSensingRange;
    protected int landArcs;

    protected double landRadius;
    protected double landLinearSpeed;
    protected double landTurnSpeed;
    protected double flyingLinearSpeed;
    protected double flyingLinearAcc;
    protected double flyingAngSpeed;
    protected double flyingAngAcc;
    protected double flyingMaxHeight;
    protected boolean useFlyingRobot;

    protected Double2D arenaSize;
    protected Double2D[] items;
    protected double itemPlacementZone;
    protected double itemRadius;
    
    protected Double2D flyingStartPos;
    protected Double2D landStartPos;
    protected double flyingStartHeight;
    protected double flyingStartOri;
    protected double landStartOri;
            
    protected boolean flyingVerticalMovement;

}
