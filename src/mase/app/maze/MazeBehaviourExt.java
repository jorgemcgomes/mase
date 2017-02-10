/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.util.ArrayList;
import java.util.LinkedList;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeBehaviourExt extends MasonEvaluation {

    private VectorBehaviourResult vbr;
    private LinkedList<Double2D> positions;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        positions = new LinkedList<Double2D>();
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        MazeTask mt = (MazeTask) sim;
        positions.add(mt.agent.getLocation());
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        MazeTask mt = (MazeTask) sim;
        while (positions.size() < maxEvaluationSteps) {
            positions.add(mt.agent.getLocation());
        }
        double[] vec = new double[positions.size() * 2];
        int index = 0;
        for(Double2D p : positions) {
            vec[index++] =  p.x;
            vec[index++] =  p.y;
        }
        vbr = new VectorBehaviourResult(vec);
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }
}
