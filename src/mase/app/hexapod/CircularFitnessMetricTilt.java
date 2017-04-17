/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.hexapod;

import mase.evaluation.FitnessResult;

/**
 * Expected in value-indexes: X final position, Y, final position, orientation, max tilt
 * @author jorge
 */
public class CircularFitnessMetricTilt extends CircularFitnessMetric {
    
    private static final long serialVersionUID = 1L;

    @Override
    public void setValues(double[] v) {
        super.setValues(v);
        double tilt = v[3];
        double tiltScore = Math.max(0, (90 - tilt) / 90);
        this.res = new FitnessResult(res.value() + tiltScore);
    }
}
