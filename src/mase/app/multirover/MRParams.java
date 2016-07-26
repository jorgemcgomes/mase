/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import mase.mason.ParamUtils.IgnoreParam;
import mase.mason.ParamUtils.MultiParam;

/**
 *
 * @author jorge
 */
public class MRParams implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    protected static final int TYPE_BINARY = 0;
    protected static final int TYPE_DISCRETISED = 1;
    
    protected double size;
    protected double startSize;
    protected double discretization;
    protected int numAgents;
    protected double linearSpeed;
    protected double turnSpeed;
    protected double rockSensorRange;
    protected double roverSensorRange;
    protected double agentRadius;
    protected String[] rocks;
    protected int numRockTypes;
    protected int minActivationTime;
    protected int typeSensorMode;
    
    @MultiParam(base="type")
    protected Double[] radius;
    @MultiParam(base="type")
    protected Integer[] time;
    @MultiParam(base="type")
    protected Color[] color;
    @MultiParam(base="type")
    protected int[][] actuators;
    
    @IgnoreParam
    protected int numActuators;
    @IgnoreParam
    protected List<RockType> rockDistribution;
    @IgnoreParam
    protected List<RockType> usedTypes;
    @IgnoreParam
    protected List<Integer> usedTypesFrequency;
            
    protected double actuatorNoise = 0; // percentage
    protected double sensorRangeNoise = 0; // percentage
    protected double sensorAngleNoise = 0; // radians
    protected double sensorOffset = 0; // percentage
    protected double actuatorOffset = 0; // percentage
    
    
    @Override
    public MRParams clone() {
        try {
            return (MRParams) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }        
}
