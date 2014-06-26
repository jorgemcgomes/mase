/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import mase.controllers.AgentController;
import mase.mason.world.EmboddiedAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PredatorHomo extends Predator {

    public PredatorHomo(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, ac);
    }

    @Override
    public double[] readNormalisedSensors() {
        PredatorPrey pp = (PredatorPrey) sim;
        // builds auxiliary list
        ArrayList<EmboddiedAgent> agents = new ArrayList<EmboddiedAgent>(pp.predators.size());
        /*for (Predator p : pp.predators) {
         if (p != this) {
         agents.add(p);
         }
         }*/

        ArrayList<Predator> sorted = new ArrayList<Predator>(pp.predators.size() - 1);
        for (Predator k : pp.predators) {
            if (k != this) {
                sorted.add(k);
            }
        }
        Collections.sort(sorted, new Comparator<Predator>() {
            @Override
            public int compare(Predator o1, Predator o2) {
                Double2D thisLoc = PredatorHomo.this.getLocation();
                return Double.compare(thisLoc.distance(o1.getLocation()), thisLoc.distance(o2.getLocation()));
            }
        });
        agents.addAll(sorted);

        Prey closest = pp.preys.get(0);
        double closestDist = Double.POSITIVE_INFINITY;
        for (Prey p : pp.activePreys) {
            double d = this.distanceTo(p);
            if (d < closestDist) {
                closest = p;
                closestDist = d;
            }
        }
        agents.add(closest);

        double[] input = new double[agents.size() * 2];
        int index = 0;
        // relative positions and angles of the ball, keepers and takers
        for (EmboddiedAgent a : agents) {
            input[index++] = (this.distanceTo(a) / (pp.par.size)) * 2 - 1;
            input[index++] = this.angleTo(a.getLocation()) / Math.PI;
        }
        return input;
    }
}
