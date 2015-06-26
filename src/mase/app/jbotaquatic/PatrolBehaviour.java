package mase.app.jbotaquatic;

import java.util.ArrayList;
import mase.evaluation.BehaviourResult;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.jbot.JBotEvaluation;

public class PatrolBehaviour extends JBotEvaluation {

    private boolean isSetup = false;
    /*private int[][] visited;
    private final double resolution = 1;
    private double width = 5, height = 5;
    
    private double v = 0;
    private double max = 0;*/
        
    private double distanceCenter = 0;
    private double pairwiseDistance = 0;
    private double distanceClosest = 0;
    private Vector2d center;
    private int steps = 0;
    private int numRobots = 0;
    private double diag = 0;
    
    private BehaviourResult res;

    private void setup(Simulator simulator) {
        double width = simulator.getEnvironment().getWidth();
        double height = simulator.getEnvironment().getHeight();
        diag = Math.sqrt(width * width + height * height);
        center = new Vector2d(width / 2, height / 2);
        /*visited = new int[(int) (height / resolution)][(int) (width / resolution)];
        for (int y = 0; y < visited.length; y++) {
            for (int x = 0; x < visited[y].length; x++) {
                double coordX = (x - visited[y].length / 2) * resolution;
                double coordY = (y - visited.length / 2) * resolution;
                if (!insideLines(new Vector2d(coordX, coordY), simulator)) {
                    visited[y][x] = -1;
                } else {
                    max++;
                }
            }
        }*/
        numRobots = simulator.getRobots().size();
    }
    
    

    /*public boolean insideLines(Vector2d v, Simulator sim) {
        //http://en.wikipedia.org/wiki/Point_in_polygon
        int count = 0;
        for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
            if (p.getType() == PhysicalObjectType.LINE) {
                Line l = (Line) p;
                if (l.intersectsWithLineSegment(v, new Vector2d(0, -100)) != null) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }*/

    @Override
    public void update(Simulator simulator) {
        steps++;
        if (!isSetup) {
            setup(simulator);
            isSetup = true;
        }

        ArrayList<Robot> robots = simulator.getRobots();
        for (int i = 0; i < robots.size(); i++) {
            AquaticDrone r = (AquaticDrone) robots.get(i);
            
            // visited space
            /*double x = r.getPosition().getX();
            double y = r.getPosition().getY();
            x /= resolution;
            y /= resolution;
            int indexY = (int) (y + visited.length / 2);
            if (indexY < visited.length && indexY >= 0) {
                int indexX = (int) (x + visited.length / 2);
                if (indexX < visited[indexY].length && indexX >= 0 && visited[indexY][indexX] == 0) {
                    visited[indexY][indexX] = 1;
                    v++;
                }
            }*/
            
            // distance to center
            distanceCenter += r.getPosition().distanceTo(center);
            
            // distance to closest
            double minDist = Double.POSITIVE_INFINITY;
            double avgDist = 0;
            for(int j = 0 ; j < robots.size() ; j++) {
                if(i != j) {
                    AquaticDrone r2 = (AquaticDrone) robots.get(j);
                    double dist = r.getPosition().distanceTo(r2.getPosition());
                    minDist = Math.min(minDist, dist);
                    avgDist += dist;
                }
            }
            avgDist /= (robots.size() - 1);
            distanceClosest += minDist;
            pairwiseDistance += avgDist;        
        }
    }

    @Override
    public EvaluationResult getResult() {
        if(res == null) {
            res = new VectorBehaviourResult(
                    /*(float) (v / max),*/
                    (float) (pairwiseDistance / steps / numRobots / (diag / 4)),
                    (float) (distanceClosest / steps / numRobots / (diag / 4)),
                    (float) (distanceCenter / steps / numRobots / (diag / 4))
            );
        }
        return res;
    }
}
