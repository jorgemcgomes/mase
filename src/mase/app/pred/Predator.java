/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.awt.Color;
import mase.app.pred.PredParams.SensorMode;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Predator extends SmartAgent {

    public static final double RADIUS = 1.5;
    public static final Color COLOUR = Color.RED;
    protected int captureCount = 0;

    public Predator(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        if (sim.par.collisions) {
            this.enableAgentCollisions(true);
        }

        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.predatorSpeed, sim.par.predatorRotateSpeed);
        super.addEffector(dm);
    }

    void setupSensors() {
        PredatorPrey pp = (PredatorPrey) super.sim;
        if (pp.par.sensorMode == SensorMode.arcs) {
            DistanceSensorArcs ds = new DistanceSensorArcs();
            ds.setArcs(pp.par.sensorArcs);
            ds.setRange(Double.POSITIVE_INFINITY);
            ds.setObjectTypes(Prey.class);
            super.addSensor(ds);
        } else if (pp.par.sensorMode == SensorMode.closest) {
            RangeBearingSensor rbs = new RangeBearingSensor();
            rbs.setObjects(pp.preys);
            rbs.setObjectCount(1);
            rbs.setSort(true);
            super.addSensor(rbs);
        } else if (pp.par.sensorMode == SensorMode.otherpreds) {
            RangeBearingSensor rbsPrey = new RangeBearingSensor();
            rbsPrey.setObjects(pp.preys);
            rbsPrey.setObjectCount(1);
            rbsPrey.setSort(true);
            super.addSensor(rbsPrey);

            RangeBearingSensor rbsOthers = new RangeBearingSensor();
            rbsOthers.setObjects(pp.predators);
            rbsOthers.setSort(true);
            super.addSensor(rbsOthers);
        }
    }

    @Override
    public void action(double[] output) {
        super.action(output);
        // capture preys
        PredatorPrey predSim = (PredatorPrey) sim;
        for (Prey prey : predSim.activePreys) {
            if (this.distanceTo(prey) <= predSim.par.captureDistance) {
                prey.disappear();
                this.captureCount++;
                predSim.captureCount++;
            }
        }
    }

    public int getCaptureCount() {
        return captureCount;
    }
}
