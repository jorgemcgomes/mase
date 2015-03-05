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

    public final static String P_FLYING_VISION_ANGLE = "flying-vision-angle";
    public final static String P_FLYING_ARCS = "flying-arcs";
    public final static String P_LAND_VISION_ANGLE = "land-vision-angle";
    public final static String P_LAND_SENSING_RANGE = "land-sensing-range";
    public final static String P_LAND_ARCS = "land-arcs";
    public final static String P_LAND_LINEAR_SPEED = "land-linear-speed";
    public final static String P_LAND_TURN_SPEED = "land-turn-speed";
    public final static String P_FLYING_LINEAR_SPEED = "flying-linear-speed";
    public final static String P_FLYING_LINEAR_ACC = "flying-linear-acc";
    public final static String P_FLYING_ANG_SPEED = "flying-ang-speed";
    public final static String P_FLYING_ANG_ACC = "flying-ang-acc";
    public final static String P_ITEMS = "items";
    public final static String P_PLACEMENT_ZONE = "item-placement-zone";
    public final static String P_ITEM_RADIUS = "item-radius";
    public final static String P_FLYING_RADIUS = "flying-radius";
    public final static String P_LAND_RADIUS = "land-radius";
    public final static String P_FLYING_START_POS = "flying-start-pos";
    public final static String P_LAND_START_POS = "land-start-pos";
    public final static String P_FLYING_START_ORI = "flying-start-ori";
    public final static String P_LAND_START_ORI = "land-start-ori";
    public final static String P_ARENA_SIZE = "arena-size";
    public final static String P_FLYING_VERTICAL_MOVEMENT = "flying-vertical-move";
    public final static String P_FLYING_MAX_HEIGHT = "flying-max-height";
    public final static String P_FLYING_START_HEIGHT = "flying-start-height";    
    public final static String P_USE_FLYING_ROBOT = "use-flying-robot";
    
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
