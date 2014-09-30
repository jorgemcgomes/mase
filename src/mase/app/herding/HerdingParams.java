/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

/**
 *
 * @author jorge
 */
public class HerdingParams {

    protected double arenaSize;
    protected double gateSize;
    protected double discretization;

    protected double agentRadius;
    protected double herdingRange; // used for Sheep & Fox

    protected int numSheeps;
    protected boolean activeSheep;
    protected double sheepSpeed;
    protected double placeRadius;
    protected double sheepX;
    
    protected int numFoxes;
    protected double foxSpeed;
    protected double foxX;

    protected int numShepherds;
    protected double shepherdSpeed;
    protected double shepherdSensorRange;
    protected double shepherdTurnSpeed;
    protected double shepherdSeparation;
    protected double shepherdX;

    public static final String P_ARENA_SIZE = "arena-size";
    public static final String P_GATE_SIZE = "gate-size";
    public static final String P_DISCRETIZATION = "discretization";

    public static final String P_AGENT_RADIUS = "agent-radius";
    public static final String P_HERDING_RANGE = "herding-range";

    public static final String P_NUM_SHEEPS = "num-sheeps";
    public static final String P_ACTIVE_SHEEP = "active-sheep";
    public static final String P_SHEEP_SPEED = "sheep-speed";
    public static final String P_PLACE_RADIUS = "place-radius";
    public static final String P_SHEEP_X = "sheep-x";

    public static final String P_NUM_FOXES = "num-foxes";
    public static final String P_FOX_SPEED = "fox-speed";
    public static final String P_FOX_X = "fox-x";

    public static final String P_NUM_SHEPHERDS = "num-shepherds";
    public static final String P_SHEPHERD_SPEED = "shepherd-speed";
    public static final String P_SHEPHERD_TURN_SPEED = "shepherd-turn-speed";
    public static final String P_SHEPHERD_SENSOR_RANGE = "shepherd-sensor-range";
    public static final String P_SHEPHERD_SEPARATION = "shepherd-separation";
    public static final String P_SHEPHERD_X = "shepherd-x";

}
