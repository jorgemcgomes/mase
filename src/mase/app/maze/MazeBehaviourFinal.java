/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeBehaviourFinal extends MasonEvaluation {

    private VectorBehaviourResult vbr;

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        MazeTask mt = (MazeTask) sim;
        Double2D loc = mt.agent.getLocation();
        vbr = new VectorBehaviourResult((float) loc.x, (float) loc.y);
    }

}
