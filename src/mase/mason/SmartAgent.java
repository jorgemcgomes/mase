/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.AgentController;
import java.awt.Color;
import java.util.Arrays;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class SmartAgent extends EmboddiedAgent {

    protected AgentController ac;
    protected double[] lastSensors;
    protected double[] lastAction;

    public SmartAgent(SimState sim, Continuous2D field, double radius, Color c, AgentController ac) {
        super(sim, field, radius, c);
        this.ac = ac;
    }

    @Override
    public void step(SimState state) {
        lastSensors = readNormalisedSensors();
        lastAction = ac.processInputs(lastSensors);
        action(lastAction);
    }

    public abstract double[] readNormalisedSensors();

    public abstract void action(double[] output);

    public double[] lastOutputs() {
        return lastAction;
    }

    public double[] lastInputs() {
        return lastSensors;
    }

    public String getActionReport() {
        return getRawActions();
    }

    public String getSensorsReport() {
        return getRawSensors();
    }

    public AgentController getAgentController() {
        return ac;
    }

    public String getRawSensors() {
        return Arrays.toString(lastSensors);
    }

    public String getRawActions() {
        return Arrays.toString(lastAction);
    }
}
