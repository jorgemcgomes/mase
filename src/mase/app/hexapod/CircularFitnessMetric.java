/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.hexapod;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.vrep.VRepEvaluationFunction;
import sim.util.Double2D;

/**
 * Expected in value-indexes: X final position, Y, final position, orientation
 * @author jorge
 */
public class CircularFitnessMetric extends VRepEvaluationFunction {

    private static final long serialVersionUID = 1L;
    protected FitnessResult res;

    @Override
    public void setValues(double[] v) {
        super.setValues(v);
        double x = v[0];
        double y = v[1];
        double ori = v[2];
        double fitness = calculateOrientationFitness(new Double2D(x,y), ori);
        res = new FitnessResult(fitness);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    public static double calculateOrientationFitness(Double2D pos, double orientation) {
        double result = getTargetOrientation(pos) - modPI2(orientation);
        result = modPI(result);
        result = Math.abs((Math.PI - result) / (Math.PI));
        return result;
    }

    public static double getTargetOrientation(Double2D pos) {
        double b = Math.sqrt((pos.x / 2.0) * (pos.x / 2.0) + (pos.y / 2.0) * (pos.y / 2.0));
        double alpha = Math.atan2(pos.y, pos.x);
        double a = b / Math.cos(alpha);
        double beta = Math.atan2(pos.y, pos.x - a);
        double orientation = 0;

        if (pos.x < 0) {
            orientation = beta - Math.PI;
        } else {
            orientation = beta;
        }

        orientation = modPI(orientation);

        return orientation;
    }

    public static double modPI(double angle) {
        while (angle < 0) {
            angle += 2.0 * Math.PI;
        }

        while (angle > 2.0 * Math.PI) {
            angle -= 2.0 * Math.PI;
        }

        return angle;
    }

    public static double modPI2(double angle) {
        while (angle < -Math.PI) {
            angle += 2.0 * Math.PI;
        }

        while (angle > Math.PI) {
            angle -= 2.0 * Math.PI;
        }

        return angle;
    }
}
