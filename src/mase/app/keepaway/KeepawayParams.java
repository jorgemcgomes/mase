/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class KeepawayParams {

    // World
    public static final String P_SIZE = "view-size";
    public static final String P_RING_SIZE = "ring-size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_BALL_DECAY = "ball-decay"; // linear decay
    public static final String P_COLLISIONS = "collisions";
    protected double size;
    protected double discretization;
    protected double ringSize;
    protected double ballDecay;
    protected boolean collisions;
    
    // Keepers
    public static final String P_NUM_KEEPERS = "n-keepers";
    public static final String P_KEEPER = "keeper";
    public static final String P_MOVE_SPEED = "move-speed";
    public static final String P_PASS_SPEED = "pass-speed";
    public static final String P_COLOR = "color";
    protected int numKeepers;
    protected double[] moveSpeed;
    protected double[] passSpeed;
    protected Color[] color;
    
    // Takers
    public static final String P_NUM_TAKERS = "n-takers";
    public static final String P_TAKER_SPEED = "taker-speed";
    public static final String P_TAKERS_PLACEMENT = "takers-placement";
    public static final String V_RANDOM = "random-center", V_CENTER = "center";
    public static final String P_PLACE_RADIUS = "placement-radius";
    protected double takerSpeed;
    protected String takersPlacement;
    protected double placeRadius;
}
