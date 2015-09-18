/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.jbotaquatic;

import environment.ForagingEnvironment;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.jbot.JBotEvaluation;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;

/**
 *
 * @author jorge
 */
public class ForagingBehaviour extends JBotEvaluation {

    private VectorBehaviourResult br;
    private double dispersion;
    private double proximity;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    public void update(Simulator simulator) {
        ForagingEnvironment env = (ForagingEnvironment) simulator.getEnvironment();

        dispersion += simulator.getRobots().get(0).getPosition().distanceTo(simulator.getRobots().get(1).getPosition());

        for (Robot r : simulator.getRobots()) {
            double closest = Double.POSITIVE_INFINITY;
            for (Vector2d w : env.getWaypoints()) {
                closest = Math.min(closest, r.getPosition().distanceTo(w));
            }
            if (!Double.isInfinite(closest)) {
                proximity += closest;
            }
        }

        br = new VectorBehaviourResult(env.getCaptureCount() / (float) env.getMaxNumItems(),
                (float) (dispersion / simulator.getTime() / env.getWidth()),
                (float) (proximity / simulator.getRobots().size() / simulator.getTime() / (env.getWidth() / 2))
        );
    }

}
