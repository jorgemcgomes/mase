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
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SmartAgent extends EmboddiedAgent {

    private static final long serialVersionUID = 1L;

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

    public int getNInputs() {
        if (lastNormSensors != null) {
            return lastNormSensors.length;
        } else {
            int c = 0;
            for (Sensor s : sensors) {
                c += s.valueCount();
            }
            return c;
        }
    }

    public int getNOutputs() {
        if (lastActionOutput != null) {
            return lastActionOutput.length;
        } else {
            int c = 0;
            for (Effector s : effectors) {
                c += s.valueCount();
            }
            return c;
        }
    }

    public void addSensor(Sensor s) {
        sensors.add(s);
    }

    public void addEffector(Effector e) {
        effectors.add(e);
    }

    @Override
    public void step(SimState state) {
        if (ac != null) {
            lastNormSensors = readNormalisedSensors();
            lastActionOutput = ac.processInputs(lastNormSensors);
            if(lastActionOutput != null) { // if output is null, do nothing
                for (int i = 0; i < lastActionOutput.length; i++) {
                    if (Double.isNaN(lastActionOutput[i]) || Double.isInfinite(lastActionOutput[i])) {
                        lastActionOutput[i] = 0.5;
                    }
                }
                action(lastActionOutput);
            }
        }
    }

    public double[] readNormalisedSensors() {
        if (lastRawSensors == null) {
            int count = getNInputs();
            lastRawSensors = new double[count];
            lastNormSensors = new double[count];
        }

        int index = 0;
        for (Sensor s : sensors) {
            double[] raw = s.readValues();
            double[] norm = s.normaliseValues(raw);
            System.arraycopy(raw, 0, lastRawSensors, index, raw.length);
            System.arraycopy(norm, 0, lastNormSensors, index, norm.length);
            index += raw.length;
        }
        return lastNormSensors;
    }

    public void action(double[] output) {
        if (effectors.size() == 1) {
            effectors.get(0).action(output);
        } else {
            int index = 0;
            for (Effector e : effectors) {
                double[] o = Arrays.copyOfRange(output, index, index + e.valueCount());
                index += e.valueCount();
                e.action(o);
            }
        }
    }

    public AgentController getAgentController() {
        return ac;
    }

    public double[] lastNormalisedOutputs() {
        return lastActionOutput;
    }

    public double[] lastNormalisedInputs() {
        return lastNormSensors;
    }

    public double[] lastRawInputs() {
        return lastRawSensors;
    }

    public String getActionReport() {
        return getRawActionsReport();
    }

    public String getSensorsReport() {
        return formatSensors(lastRawSensors);
    }

    public String getRawSensorsReport() {
        return formatSensors(lastNormSensors);
    }

    private String formatSensors(double[] values) {
        if(values == null) {
            return "NOT STARTED";
        }
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for (Sensor s : sensors) {
            sb.append(s.getClass().getSimpleName()).append("{");
            for (int i = 0; i < s.valueCount(); i++) {
                sb.append("[").append(i).append("]").append(String.format("%.2f", values[index++])).append(" ");
            }
            sb.setCharAt(sb.length() - 1, '}');
        }
        return sb.toString();
    }

    public String getRawActionsReport() {
        if(lastActionOutput == null) {
            return "NOT STARTED";
        }
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for (Effector e : effectors) {
            sb.append(e.getClass().getSimpleName()).append("{");
            for (int i = 0; i < e.valueCount(); i++) {
                sb.append("[").append(i).append("]").append(String.format("%.2f", lastActionOutput[index++])).append(" ");
            }
            sb.setCharAt(sb.length() - 1, '}');
        }
        return sb.toString();
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public List<Effector> getEffectors() {
        return effectors;
    }

    @Override
    public void setLocation(Double2D loc) {
        super.setLocation(loc); 
        for(Sensor s : sensors) {
            field.setObjectLocation(s, loc);
        }
        for(Effector e : effectors) {
            field.setObjectLocation(e, loc);
        }
    }    

    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        Inspector defaultInspector = super.getInspector(wrapper, state);
        defaultInspector.setTitle("Agent Properties");
        Inspector controllerInspector = Inspector.getInspector(ac, state, "Agent Controller");
        controllerInspector.setTitle("Agent Controller");
        
        TabbedInspector tab = new TabbedInspector("Agent Inspector");
        tab.addInspector(defaultInspector);
        tab.addInspector(controllerInspector);
        return tab;
    }
}
