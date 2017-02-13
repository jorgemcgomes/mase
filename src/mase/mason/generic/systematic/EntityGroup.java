/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic.systematic;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jorge
 */
public class EntityGroup {

    private final int minSize, maxSize;
    private final boolean isStatic;
    private int varsLen = -1;
    private List<? extends Entity> ents;

    public EntityGroup(List<? extends Entity> ents, int min, int max, boolean isStatic) {
        this.ents = ents;
        this.minSize = min;
        this.maxSize = max;
        this.isStatic = isStatic;
    }

    public EntityGroup(List<? extends Entity> ents, boolean isStatic) {
        this(ents, ents.size(), ents.size(), isStatic);
    }
    
    public EntityGroup (List<? extends Entity> ents) {
        this(ents, false);
    }


    public double[] getAverageState() {
        if (varsLen == -1) {
            varsLen = ents.get(0).getStateVariables().length;
        }
        double[] res = new double[varsLen];
        if (ents.isEmpty()) {
            Arrays.fill(res, Double.NaN);
        } else {
            for (Entity a : ents) {
                double[] state = a.getStateVariables();
                for (int i = 0; i < state.length; i++) {
                    res[i] += state[i] / ents.size();
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

    public List<? extends Entity> getEntities() {
        return ents;
    }
    
    public int size() {
        return ents.size();
    }
}
