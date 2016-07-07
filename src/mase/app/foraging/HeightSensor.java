/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.mason.world.AbstractSensor;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class HeightSensor extends AbstractSensor {

    private FlyingEffector fe;
    private double noise = 0;

    public HeightSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    @Override
    public int valueCount() {
        return 1;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    @Override
    public double[] readValues() {
        double h = fe.getHeight();
        h += (state.random.nextDouble() * 2 - 1) * noise * fe.getMaxHeight();
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
