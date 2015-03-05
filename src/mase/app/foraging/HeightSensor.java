/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.mason.world.AbstractSensor;

/**
 *
 * @author jorge
 */
public class HeightSensor extends AbstractSensor {

    private FlyingEffector fe;
    
    @Override
    public int valueCount() {
        return 1;
    }

    @Override
    public double[] readValues() {
        double h = fe.getHeight();
        return new double[]{h};
    }

    @Override
    public double[] normaliseValues(double[] vals) {
        double nh = vals[0] / fe.getMaxHeight() * 2 - 1;
        return new double[]{nh};
    }
    
    public void setFlyingEffector(FlyingEffector fe) {
        this.fe = fe;
    }
    
}
