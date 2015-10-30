/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.controllers.AgentController;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SmartAgent extends EmboddiedAgent {

    protected AgentController ac;
    protected double[] lastRawSensors;
    protected double[] lastNormSensors;
    protected double[] lastActionOutput;
    protected List<Sensor> sensors;
    protected List<Effector> effectors;
    
    public SmartAgent(SimState sim, Continuous2D field, double radius, Color c, AgentController ac) {
        super(sim, field, radius, c);
        this.ac = ac;
        this.sensors = new ArrayList<>();
        this.effectors = new ArrayList<>();
    }
    
    public void addSensor(Sensor s) {
        s.setAgent(sim, field, this);
        sensors.add(s);
    }
    
    public void addEffector(Effector e) {
        e.setAgent(sim, field, this);
        effectors.add(e);
    }

    @Override
    public void step(SimState state) {
        if(ac != null) {
            lastNormSensors = readNormalisedSensors();
            lastActionOutput = ac.processInputs(lastNormSensors);
            for(int i = 0 ; i < lastActionOutput.length ; i++) {
                if(Double.isNaN(lastActionOutput[i]) || Double.isInfinite(lastActionOutput[i])) {
                    lastActionOutput[i] = 0.5;
                }
            }
            action(lastActionOutput);
        }
    }

    public double[] readNormalisedSensors() {
        if(lastRawSensors == null) {
            int count = 0;
            for(Sensor s : sensors) {
                count += s.valueCount();
            }
            lastRawSensors = new double[count];
            lastNormSensors = new double[count];
        } 
        
        int index = 0;
        for(Sensor s : sensors) {
            double[] raw = s.readValues();
            double[] norm = s.normaliseValues(raw);
            s.normaliseValues(raw);
            System.arraycopy(raw, 0, lastRawSensors, index, raw.length);
            System.arraycopy(norm, 0, lastNormSensors, index, norm.length);
            index += raw.length;
        }
        return lastNormSensors;
    }

    public void action(double[] output) {
        if(effectors.size() == 1) {
            effectors.get(0).action(output);
        } else {
            int index = 0;
            for(Effector e : effectors) {
                double[] o = Arrays.copyOfRange(output, index, index + e.valueCount());
                index += e.valueCount();
                e.action(o);
            }
        }
    }

    public AgentController getAgentController() {
        return ac;
    }    
    
    public double[] lastOutputs() {
        return lastActionOutput;
    }

    public double[] lastInputs() {
        return lastNormSensors;
    }
    
    public double[] lastRawInputs() {
        return lastRawSensors;
    }

    public String getActionReport() {
        return getRawActions();
    }

    public String getSensorsReport() {
        return Arrays.toString(lastRawSensors);
    }

    public String getRawSensors() {
        return Arrays.toString(lastNormSensors);
    }

    public String getRawActions() {
        return Arrays.toString(lastActionOutput);
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public List<Effector> getEffectors() {
        return effectors;
    }
    
    
}
