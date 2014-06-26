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
    public static final Color COLOUR = Color.GREEN;
    protected int captureCount = 0;

    public Predator(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        if (sim.par.collisions) {
            this.enableCollisionDetection(true);
        }

        if (sim.par.sensorMode == SensorMode.arcs) {
            DistanceSensorArcs ds = new DistanceSensorArcs();
            ds.setArcs(sim.par.sensorArcs);
            ds.setRange(Double.POSITIVE_INFINITY);
            ds.setObjectTypes(Prey.class);
            super.addSensor(ds);
        } else if (sim.par.sensorMode == SensorMode.closest) {
            RangeBearingSensor rbs = new RangeBearingSensor();
            rbs.setObjects(sim.preys);
            rbs.setObjectCount(1);
            rbs.setSort(true);
            super.addSensor(rbs);
        } else if (sim.par.sensorMode == SensorMode.otherpreds) {
            RangeBearingSensor rbsOthers = new RangeBearingSensor();
            rbsOthers.setObjects(sim.predators);
            rbsOthers.setSort(true);
            super.addSensor(rbsOthers);

            RangeBearingSensor rbsPrey = new RangeBearingSensor();
            rbsPrey.setObjects(sim.preys);
            rbsPrey.setObjectCount(1);
            rbsPrey.setSort(true);
            super.addSensor(rbsPrey);
        }

        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.predatorSpeed, sim.par.predatorRotateSpeed);
        super.addEffector(dm);
    }

    @Override
    public void action(double[] output) {
        super.action(output);

        // capture preys
        PredatorPrey predSim = (PredatorPrey) sim;
        Bag objects = field.getNeighborsExactlyWithinDistance(getLocation(), predSim.par.captureDistance + Predator.RADIUS + Prey.RADIUS, false);
        for (Object o : objects) {
            if (o instanceof Prey) {
                ((Prey) o).disappear();
                predSim.td.groups()[1].remove((Prey) o);
                this.captureCount++;
                predSim.captureCount++;
            }
        }
    }

    public int getCaptureCount() {
        return captureCount;
    }
}
