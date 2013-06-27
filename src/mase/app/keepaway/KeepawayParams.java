/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class KeepawayParams {

    public static final String P_SIZE = "view-size";
    public static final String P_RING_SIZE = "ring-size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_KEEPER_SPEED = "keeper-speed";
    public static final String P_NUM_KEEPERS = "n-keepers";
    public static final String P_NUM_TAKERS = "n-takers";
    public static final String P_TAKER_SPEED = "taker-speed";
    public static final String P_BALL_SPEED = "ball-speed";
    public static final String P_BALL_DECAY = "ball-decay"; // linear decay
    public static final String P_TAKERS_PLACEMENT = "takers-placement";
    public static final String V_RANDOM = "random-center", V_CENTER = "center";
    public static final String P_PLACE_RADIUS = "placement-radius";
    public static final String P_KEEPERS_PLACEMENT = "keepers-placement";
    public static final String P_BALL_PLACEMENT = "ball-placement";
    public static final String P_COLLISIONS = "collisions";
    
    protected double size;
    protected double discretization;
    protected double ringSize;
    protected double keeperSpeed;
    protected double takerSpeed;
    protected int numKeepers;
    protected double ballSpeed;
    protected double ballDecay;
    protected String takersPlacement;
    protected double placeRadius;
    protected boolean collisions;
    
}
