/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.gp.GPData;
import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public class Data extends GPData {

    private static final long serialVersionUID = 1L;
    
    protected boolean boolValue = false;
    protected double doubleValue = Double.NaN;
    protected int primitive = Integer.MAX_VALUE;
    protected double[] sensorValues = null;  // the current sensor values
    protected AgentController ac = null; // the current agent controller

    @Override
    public void copyTo(GPData gpd) {
        super.copyTo(gpd);
        Data d = (Data) gpd;
        d.boolValue = boolValue;
        d.doubleValue = doubleValue;
        d.sensorValues = sensorValues; // it's read-only, so it's safe
        d.primitive = primitive;
        d.ac = ac;
    }
    
    
}
