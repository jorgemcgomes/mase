/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.util.Arrays;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class TestPredator extends Predator {

    private double[] sensors;
    private double[] normalizedSensors;

    public TestPredator(PredatorPrey sim, Continuous2D field, double x, double y, double orientation) {
        super(sim, field, null);
    }

    @Override
    public void step(SimState state) {
        if (predSim.activePreys.isEmpty()) {
            return;
        }

        /* Sensors test */
        Prey closest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (Prey p : predSim.activePreys) {
            double d = this.getLocation().distance(p.getLocation());
            if (d < distance) {
                distance = d;
                closest = p;
            }
        }
        double maxDistance = Math.sqrt(Math.pow(predSim.par.size, 2) * 2);
        normalizedSensors = new double[2];
        sensors = new double[2];
        normalizedSensors[0] = (distance / maxDistance) * 2 - 1;
        sensors[0] = distance;

        Double2D predatorToPrey = closest.getLocation().subtract(getLocation()).normalize();
        double angle = Math.atan2(predatorToPrey.y, predatorToPrey.x) - Math.atan2(getDirection().y, getDirection().x);
        if (Math.abs(angle) > Math.PI) {
            angle -= Math.PI * 2;
        }
        normalizedSensors[1] = angle / Math.PI;
        sensors[1] = angle;

        /* Chase nearest prey */
        //Double2D captureVec = closest.getLocation().subtract(pos);
        //super.move(captureVec, predatorSpeed);
        super.move(new Double2D(0, 1), predSim.par.predatorSpeed);

        // capture preys
        Bag objects = field.getObjectsExactlyWithinDistance(getLocation(), predSim.par.captureDistance, false);
        for (Object o : objects) {
            if (o instanceof Prey) {
                ((Prey) o).disappear();
                predSim.captureCount++;
            }
        }
    }

    public String getSensorReport() {
        return Arrays.toString(sensors);
    }

    public String getNormalizedSensorReport() {
        return Arrays.toString(normalizedSensors);
    }
}
