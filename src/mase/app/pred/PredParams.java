/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredParams {

    public static final String P_SIZE = "size";
    public static final String P_DISCRETIZATION = "discretization";
    public static final String P_PREY_SPEED = "prey-speed";
    public static final String P_ESCAPE_DISTANCE = "escape-distance";
    public static final String P_PREDATOR_SPEED = "predator-speed";
    public static final String P_PREDATOR_ROTATE_SPEED = "predator-rotate-speed";
    public static final String P_CAPTURE_DISTANCE = "capture-distance";
    public static final String P_PREDATOR_SEPARATION = "predator-separation";
    public static final String P_PREY_MARGIN = "prey-margin";
    public static final String P_PREY_SEPARATION = "prey-separation";
    public static final String P_NPREYS = "n-preys";
    public static final String P_NPREDATORS = "n-predators";
    public static final String P_PREY_PLACEMENT = "prey-placement";
    public static final String V_RANDOM = "random", V_CENTER = "center", V_RANDOM_CENTER = "random-center";
    public static final String P_ESCAPE_STRATEGY = "escape-strategy";
    public static final String V_MEAN_VECTOR = "mean-vector", V_NEAREST = "nearest";
    public static final String P_SENSOR_MODE = "sensor-mode";
    public static final String V_CLOSEST = "closest", V_ARCS = "arcs";
    public static final String P_SENSOR_ARCS = "n-arcs";
    public static final String P_COLLISIONS = "collisions";
    public double size = 100d;
    public double discretization = 10d;
    public double preySpeed = 1d;
    public double preyMargin = 5d;
    public double escapeDistance = 10d;
    public double predatorSpeed = 1d;
    public double predatorRotateSpeed = Math.PI / 2;
    public double captureDistance = 5d;
    public double predatorSeparation = 20d;
    public double preySeparation = 5d;
    public int nPreys = 1;
    public int nPredators = 3;
    public String preyPlacement = V_CENTER;
    public String escapeStrategy = V_MEAN_VECTOR;
    public String sensorMode = V_CLOSEST;
    public int sensorArcs;
    public boolean collisions;
}
