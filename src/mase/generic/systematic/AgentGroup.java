/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import java.util.ArrayList;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class AgentGroup extends ArrayList<Agent> {

    public Double2D getCentreOfMass() {
        int al = countAlive();
        if (al == 0) {
            return null;
        }
        MutableDouble2D centreMass = new MutableDouble2D();
        for (Agent a : this) {
            if (a.isAlive()) {
                centreMass.addIn(a.getPosition());
            }
        }
        centreMass.multiplyIn(1.0 / al);
        return new Double2D(centreMass);
    }

    public double[] getAverageState() {
        int al = countAlive();
        if (al == 0) {
            return null;
        }
        double[] res = null;
        for (Agent a : this) {
            if (a.isAlive()) {
                double[] state = a.getStateVariables();
                if (res == null) {
                    res = new double[state.length];
                }
                for (int i = 0; i < state.length; i++) {
                    res[i] += state[i] / al;
                }
            }
        }
        return res;
    }

    public int countAlive() {
        int al = 0;
        for (Agent a : this) {
            if (a.isAlive()) {
                al++;
            }
        }
        return al;
    }

    public double distanceToGroup(AgentGroup other) {
        int c = 0;
        double dist = 0;
        for (Agent a : this) {
            if (a.isAlive()) {
                for (Agent otherA : other) {
                    if (otherA.isAlive()) {
                        dist += a.getPosition().distance(otherA.getPosition());
                        c++;
                    }
                }
            }
        }
        return c == 0 ? Double.NaN : dist / c;
    }
}
