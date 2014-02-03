/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jorge
 */
public class EntityGroup extends ArrayList<Entity> {

    private final int minSize, maxSize;
    private final boolean isStatic;
    private int size = -1;

    public EntityGroup(List<? extends Entity> ents, int min, int max, boolean isStatic) {
        super(ents);
        this.minSize = min;
        this.maxSize = max;
        this.isStatic = isStatic;
    }

    public EntityGroup(List<Entity> ents, boolean isStatic) {
        this(ents, ents.size(), ents.size(), isStatic);
    }


    public double[] getAverageState() {
        if (size == -1) {
            size = this.get(0).getStateVariables().length;
        }
        double[] res = new double[size];
        if (this.isEmpty()) {
            Arrays.fill(res, Double.NaN);
        } else {
            for (Entity a : this) {
                double[] state = a.getStateVariables();
                for (int i = 0; i < state.length; i++) {
                    res[i] += state[i] / this.size();
                }
            }
        }
        //System.out.println(Arrays.toString(res));
        return res;
    }

    /*public double distanceToGroup(EntityGroup other) {
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
     }*/
    public int getMinSize() {
        return minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
