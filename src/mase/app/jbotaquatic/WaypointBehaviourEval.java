/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.jbotaquatic;

import commoninterface.entities.Waypoint;
import evaluation.WaypointFitness;
import java.util.ArrayList;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.jbot.JBotEvaluation;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.AquaticDrone;

/**
 *
 * @author jorge
 */
public class WaypointBehaviourEval extends JBotEvaluation {

    private int steps = 0;
    private double travelled = 0;
    private double finalDist = 0;
    private double meanDist = 0;
    private double meanDistClosest = 0;
    private double startingDist = 0;
    private double diag = 0;
    private boolean configured = false;
    private Waypoint wp = null;
    private Vector2d lastPos = null;
    private VectorBehaviourResult result = null;

    @Override
    public void update(Simulator simulator) {
        steps++;
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        if (!configured) {
            ArrayList<Waypoint> waypoints = Waypoint.getWaypoints(drone);
            if (!waypoints.isEmpty()) {
                wp = drone.getActiveWaypoint();
                startingDist = WaypointFitness.calculateDistance(wp, drone);
            }
            configured = true;
            lastPos = new Vector2d(drone.getPosition());
            diag = FastMath.sqrt(FastMath.pow2(simulator.getEnvironment().getWidth()) + FastMath.pow2(simulator.getEnvironment().getHeight()));
        }

        if (wp != null) {
            finalDist = WaypointFitness.calculateDistance(wp, drone);
            meanDist += finalDist;
            
            double closest = Double.POSITIVE_INFINITY;
            for (int i = 1; i < simulator.getRobots().size(); i++) {
                AquaticDrone other = (AquaticDrone) simulator.getRobots().get(i);
                double dist = drone.getPosition().distanceTo(other.getPosition());
                closest = Math.min(dist, closest);
            }
            meanDistClosest += closest;
            
            travelled += drone.getPosition().distanceTo(lastPos);
            lastPos = new Vector2d(drone.getPosition());
            
        }
    }

    @Override
    public EvaluationResult getResult() {
        if(result == null) {
            result = new VectorBehaviourResult(
                    (float) (finalDist / startingDist),
                    (float) (meanDist / startingDist / steps),
                    (float) (travelled / diag),
                    (float) (meanDistClosest / steps / (diag / 10))
            );
        }
        return result;
    }

}
