/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import mase.mason.EmboddiedAgent;
import mase.mason.EnvironmentalFeature;

/**
 *
 * @author jorge
 */
public class StaticEntity implements PhysicalEntity {

    public static final double[] EMPTY_ARRAY = new double[]{};
    private final EnvironmentalFeature geom;

    public StaticEntity(EnvironmentalFeature ef) {
        this.geom = ef;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public double[] getStateVariables() {
        return EMPTY_ARRAY;
    }

    @Override
    public double distance(PhysicalEntity other) {
        if (other instanceof EmboddiedAgent) {
            EmboddiedAgent o = (EmboddiedAgent) other;
            return Math.max(0, geom.distanceTo(o.getLocation()) - o.getRadius());
        } else {
            return 0;
        }
    }

}
