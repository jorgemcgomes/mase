/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.awt.Color;
import mase.controllers.AgentController;
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
    protected PredatorPrey predSim;
    protected int captureCount = 0;
    protected double maxDistance;

    public Predator(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        this.predSim = sim;
        this.maxDistance = Math.sqrt(Math.pow(sim.par.size, 2) * 2);
        if (sim.par.collisions) {
            this.enableCollisionDetection(true);
        }
    }

    @Override
    public double[] readNormalisedSensors() {
        if (predSim.activePreys.isEmpty()) {
            return new double[]{0, 0};
        }

        Prey closest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (Prey p : predSim.activePreys) {
            double d = getLocation().distance(p.getLocation());
            if (d < distance) {
                distance = d;
                closest = p;
            }
        }
        double[] sensors = new double[2];
        sensors[0] = Math.min(1, (distance / maxDistance) * 2 - 1);
        double angle = this.angleTo(closest.getLocation());
        sensors[1] = angle / Math.PI;

        return sensors;
    }

    @Override
    public void action(double[] output) {
        double forward = output[0] * predSim.par.predatorSpeed;
        double r = (output[1] * 2 - 1) * predSim.par.predatorRotateSpeed;
        super.move(orientation2D() + r, forward);

        // capture preys
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
