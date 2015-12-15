/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public class MRParams implements Serializable {

    private static final long serialVersionUID = 1L;
    
    protected double size;
    protected double discretization;
    protected int numAgents;
    protected double linearSpeed;
    protected double turnSpeed;
    protected double sensorRange;
    protected double rockRadius;
    protected int minActivationTime;
    protected int collectionTime;
    protected int numActuators;
    protected String[] rocks;    
}
