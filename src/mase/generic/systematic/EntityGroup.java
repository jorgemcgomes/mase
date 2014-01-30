/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class EntityGroup extends ArrayList<PhysicalEntity> {

    public double[] getAverageState() {
        double[] res = new double[this.get(0).getStateVariables().length];
        int al = countAlive();
        if (al == 0) {
            Arrays.fill(res, Double.NaN);
            return res;
        }
        for (PhysicalEntity a : this) {
            if (a.isAlive()) {
                double[] state = a.getStateVariables();
                for (int i = 0; i < state.length; i++) {
                    res[i] += state[i] / al;
                }
            }
        }
        return res;
    }

    public int countAlive() {
        int al = 0;
        for (PhysicalEntity a : this) {
            if (a.isAlive()) {
                al++;
            }
        }
        return al;
    }

    public double distanceToGroup(EntityGroup other) {
        int c = 0;
        double dist = 0;
        for (PhysicalEntity a : this) {
            if (a.isAlive()) {
                for (PhysicalEntity otherA : other) {
                    if (otherA.isAlive()) {
                        dist += a.distance(otherA);
                        c++;
                    }
                }
            }
        }
        return c == 0 ? Double.NaN : dist / c;
    }
}
