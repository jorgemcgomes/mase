/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.jbotaquatic;

import environment.PredatorPreyEnvironment;
import evaluation.PredatorPreyEvaluation;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.jbot.JBotEvaluation;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;

/**
 * Capture? ; Final distance ; time ; predator dispersion
 *
 * @author jorge
 */
public class PredatorBehaviour extends JBotEvaluation {

    private VectorBehaviourResult vbr;
    private double accumDispersion = 0;

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    @Override
    public void update(Simulator simulator) {
        AquaticDrone prey = ((PredatorPreyEnvironment) simulator.getEnvironment()).getPreyDrone();

        boolean caught = false;
        double finalDist = 0;
        double dispersion = 0;

        for (Robot r : simulator.getRobots()) {
            if (r != prey) {
                // final distance
                double d = r.getPosition().distanceTo(prey.getPosition());
                finalDist += d;

                // captured
                if (d <= PredatorPreyEvaluation.CAPTURE_DIST) {
                    caught = true;
                }
                
                // dispersion
                for (Robot r2 : simulator.getRobots()) {
                    if (r2 != prey && r2 != r) {
                        dispersion += r.getPosition().distanceTo(r2.getPosition());
                    }
                }
            }
        }
        dispersion /= ((simulator.getRobots().size() - 1) * (simulator.getRobots().size() - 2));
        accumDispersion += dispersion;
        finalDist /= (simulator.getRobots().size() - 1);
        
        vbr = new VectorBehaviourResult(
                caught ? 1 : 0,
                (float) (finalDist / simulator.getEnvironment().getWidth()),
                (float) (simulator.getTime() / simulator.getEnvironment().getSteps()),
                (float) (accumDispersion / simulator.getTime() / simulator.getEnvironment().getWidth())
        );  
    }
}
