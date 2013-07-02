/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import java.awt.Color;
import mase.AgentController;
import mase.mason.SmartAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class AggregationAgent extends SmartAgent {
    
    public static final double RADIUS = 2;

    public AggregationAgent(SimState sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
        this.enableCollisionDetection(true);
    }

    @Override
    public double[] readNormalisedSensors() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void action(double[] output) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
