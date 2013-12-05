/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.util.Iterator;
import java.util.LinkedList;
import mase.controllers.AgentController;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPredator extends Predator {

    private final double[] arcStart;
    private final double[] arcEnd;

    public MultiPredator(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, ac);
        int arcs = sim.par.sensorArcs;
        arcStart = new double[arcs];
        arcEnd = new double[arcs];
        double arcAngle = (Math.PI * 2) / arcs;
        arcStart[0] = -arcAngle / 2; // first arc aligned with front
        arcEnd[0] = arcAngle / 2;
        for (int i = 1; i < arcs; i++) {
            arcStart[i] = arcEnd[i - 1];
            arcEnd[i] = arcStart[i] + arcAngle;
            if (arcEnd[i] > Math.PI) {
                arcEnd[i] -= Math.PI * 2;
            }
        }
    }

    @Override
    public double[] readNormalisedSensors() {
        if (predSim.activePreys.isEmpty()) {
            return new double[arcStart.length * 2];
        }
        // (numero de agentes, distancia do mais proximo) para cada arco
        // 4-8 arcos -- tem que ser testado        
        double[] sensorValues = new double[arcStart.length * 2];
        LinkedList<Prey> tempList = new LinkedList<Prey>(predSim.activePreys);
        for (int i = 0; i < arcStart.length; i++) {
            double closest = Double.POSITIVE_INFINITY;
            int count = 0;
            for (Iterator<Prey> iter = tempList.iterator(); iter.hasNext();) {
                Prey p = iter.next();
                double angle = this.angleTo(p.getLocation());
                if (angle > arcStart[i] && angle < arcEnd[i]
                        || (arcStart[i] > arcEnd[i] && (angle > arcStart[i] || angle < arcEnd[i]))) {
                    closest = Math.min(closest, this.distanceTo(p));
                    count++;
                    iter.remove();
                }
            }
            sensorValues[i * 2] = (count / (double) predSim.activePreys.size()) * 2 - 1;
            sensorValues[i * 2 + 1] = count == 0 ? 1 : Math.min(1, (closest / maxDistance) * 2 - 1);
        }

        return sensorValues;
    }
}
