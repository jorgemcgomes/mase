/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.io.Serializable;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredParams implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final int V_RANDOM = 0, V_CENTER = 1;
    public static final int V_MEAN_VECTOR = 0, V_NEAREST = 1;
    public static final int V_NONE = 0, V_RBS_CLOSEST = 1, V_RBS_ALL = 2, V_ARCS = 3;

    public double size = 100d;
    public double discretization = 10d;
    public boolean collisions;
    
    public int nPreys = 1;
    public double preySpeed = 1d;
    public int escapeStrategy = V_MEAN_VECTOR;
    public double escapeDistance = 10d;
    public int preyPlacement = V_RANDOM;
    public double preyMargin = 5d;    
    public double preySeparation = 5d;
    
    public int nPredators = 3;
    public double predatorLinearSpeed = 1d;
    public double predatorTurnSpeed = Math.PI / 2;
    public double captureDistance = 5d;
    public int preySensorMode = V_RBS_CLOSEST;
    public double preySensorRange = Double.POSITIVE_INFINITY;
    public int predatorSensorMode = V_NONE;
    public double predatorSensorRange = Double.POSITIVE_INFINITY;
    public int sensorArcs;
    public double predatorSeparation = 20d;

}
