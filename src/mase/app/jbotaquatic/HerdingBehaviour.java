/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.jbotaquatic;

import commoninterface.utils.CoordinateUtilities;
import environment.HerdingEnvironment;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.jbot.JBotEvaluation;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;

/**
 *
 * @author jorge
 */
public class HerdingBehaviour extends JBotEvaluation {

    private VectorBehaviourResult vbr;
    private float shepherdDist;
    private float shepherdDispersion;

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    @Override
    public void update(Simulator simulator) {
        HerdingEnvironment env = (HerdingEnvironment) simulator.getEnvironment();
        AquaticDrone preyDrone = env.getPreyDrone();

        double disp = 0;
        double dist = 0;
        for (Robot r : simulator.getRobots()) {
            if (r != preyDrone) {
                for (Robot r2 : simulator.getRobots()) {
                    if (r2 != r && r2 != preyDrone) {
                        disp += r.getPosition().distanceTo(r2.getPosition());
                    }
                }
                dist += r.getPosition().distanceTo(preyDrone.getPosition());
            }
        }
        disp /= (simulator.getRobots().size() - 1) * (simulator.getRobots().size() - 2);
        shepherdDispersion += disp;
        dist /= (simulator.getRobots().size() - 1);
        shepherdDist += dist;

        double sheepDist = env.getObjective().distanceTo(preyDrone.getPosition());

        vbr = new VectorBehaviourResult(
                (float) (simulator.getTime() / env.getSteps()),
                (float) (sheepDist / env.getWidth()),
                (float) (shepherdDispersion / simulator.getTime() / (env.getWidth() / 2)),
                (float) (shepherdDist / simulator.getTime() / (env.getWidth() / 2))
        );
    }
}
