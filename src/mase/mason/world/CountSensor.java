/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;

/**
 *
 * @author jorge
 */
public class CountSensor extends AbstractSensor {

    private double range = Double.POSITIVE_INFINITY;
    private Class<? extends SensableObject>[] types = new Class[]{SensableObject.class};
    private int max = 1;

    public CountSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setObjectTypes(Class<SensableObject>... types) {
        this.types = types;
    }

    public void setRange(double range) {
        this.range = range;
    }

    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public double[] readValues() {
        int count = 0;
        Bag neighbours = Double.isInfinite(range) ? field.allObjects
                : field.getNeighborsWithinDistance(ag.getCenterLocation(), range + ag.getRadius(), false, true);
        for (Object n : neighbours) {
            if (objectMatch(n)) {
                double dist = ((SensableObject) n).distanceTo(ag);
                if (dist <= range) {
                    count++;
                }
            }
        }
        return new double[]{count};
    }

    protected boolean objectMatch(Object o) {
        if (o == ag) {
            return false;
        }
        for (Class type : types) {
            if (type.isInstance(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        return new double[]{vals[0] / max * 2 - 1};
    }

}
