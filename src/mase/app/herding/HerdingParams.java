/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class HerdingParams implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    protected double arenaSize;
    protected double gateSize;
    protected double discretization;

    protected double agentRadius;
    protected double herdingRange; // used for both the Sheep & Fox

    protected int numSheeps;
    protected boolean activeSheep; // the sheep will run towards the open side
    protected double sheepSpeed;
    protected boolean randomSheepPosition; // only in the y-axis
    protected double sheepX;
    
    protected int numFoxes;
    protected boolean smartFox; // the fox will go towards the sheep *avoiding* the shepherds
    protected double foxSpeed;
    protected boolean randomFoxPosition; // only in the y-axis
    protected double foxX;

    protected int numShepherds;
    protected double shepherdLinearSpeed;
    protected double shepherdTurnSpeed;
    protected double shepherdSensorRange; // used for all sensors where applicable
    protected boolean shepherdArcSensor; // for the fox sensor, if false uses RangeBearingSensor
    protected double shepherdSeparation; // in the y-axis
    protected double shepherdX;
    
    protected double actuatorNoise = 0; // percentage
    protected double sensorRangeNoise = 0; // percentage
    protected double sensorAngleNoise = 0; // radians
    protected double sensorOffset = 0; // percentage
    protected double actuatorOffset = 0; // percentage
    protected double sheepPositionOffset = 0; // max. absolute deviation

    @Override
    public HerdingParams clone() {
        try {
            return (HerdingParams) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }    
    
}
