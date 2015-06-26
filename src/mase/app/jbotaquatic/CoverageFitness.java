package mase.app.jbotaquatic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.jbot.JBotEvaluation;

public class CoverageFitness extends JBotEvaluation {

    private boolean isSetup = false;
    private double[][] coverage;
    private double resolution = 5;
    private double width = 5, height = 5;
    private final double decrease = 1.0 / (10 * 100);//1000 steps to go from 1.0 to 0.0

    private double max = 0;
    private double distance = 25;
    private double fitness;
    int steps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        resolution = state.parameters.getDouble(base.push("resolution"), null);
        distance = state.parameters.getDouble(base.push("distance"), null);
    }

    public void setup(Simulator simulator) {
        width = simulator.getEnvironment().getWidth();
        height = simulator.getEnvironment().getHeight();
        coverage = new double[(int) (height / resolution)][(int) (width / resolution)];
        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {
                double coordX = (x - coverage[y].length / 2) * resolution;
                double coordY = (y - coverage.length / 2) * resolution;
                if (!insideLines(new Vector2d(coordX, coordY), simulator)) {
                    coverage[y][x] = -1;
                } else {
                    max++;
                }
            }
        }
    }

    public boolean insideLines(Vector2d v, Simulator sim) {
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
    }

    @Override
    public void update(Simulator simulator) {
        if (!isSetup) {
            setup(simulator);
            isSetup = true;
            steps = simulator.getEnvironment().getSteps();
        }

        updateAll();

        ArrayList<Robot> robots = simulator.getRobots();

        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {

                if (coverage[y][x] == -1) {
                    continue;
                }

                double px = (x - coverage[y].length / 2) * resolution;
                double py = (y - coverage.length / 2) * resolution;

                Vector2d p = new Vector2d(px, py);

                for (Robot r : robots) {
                    if (p.distanceTo(r.getPosition()) < distance && insideLines(r.getPosition(), simulator)) {
                        coverage[y][x] = 1.0;
                    }
                }
            }
        }

//		fitness = (v/max - penalty)/robots.size();
        //fitness = countAll() / max;
        fitness += (countAll() / max) / steps;
//		printGrid();
    }

    private void updateAll() {
        for (int i = 0; i < coverage.length; i++) {
            for (int j = 0; j < coverage[i].length; j++) {
                if (coverage[i][j] > 0) {
                    if (coverage[i][j] <= 1) {
                        coverage[i][j] -= decrease;
                        if (coverage[i][j] < 0) {
                            coverage[i][j] = 0;
                        }
                    }
                }
            }
        }
    }

    private double countAll() {

        double sum = 0;

        for (int i = 0; i < coverage.length; i++) {
            for (int j = 0; j < coverage[i].length; j++) {
                if (coverage[i][j] > 0) {
                    sum += coverage[i][j];
                }
            }
        }

        return sum;
    }

    @Override
    public EvaluationResult getResult() {
        return new FitnessResult((float) fitness, FitnessResult.ARITHMETIC);
    }
}
