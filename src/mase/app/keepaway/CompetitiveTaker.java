/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.mason.EmboddiedAgent;
import mase.mason.SmartAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class CompetitiveTaker extends SmartAgent {

    private List<EmboddiedAgent> agents;

    public CompetitiveTaker(SimState sim, Continuous2D field, AgentController ac) {
        super(sim, field, Taker.RADIUS, Color.BLACK, ac);
    }

    @Override
    public double[] readNormalisedSensors() {
        Keepaway kw = (Keepaway) sim;
        // builds auxiliary list
        if (agents == null) {
            agents = new ArrayList<EmboddiedAgent>(kw.keepers.size() + 1);
            agents.add(kw.ball);
            //agents.addAll(kw.keepers);
        }

        double[] input = new double[agents.size() * 2 + 1];
        int index = 0;
        // relative positions and angles of the ball and keepers
        for (EmboddiedAgent a : agents) {
            input[index++] = (this.distanceTo(a) / (kw.par.size)) * 2 - 1;
            input[index++] = this.angleTo(a.getLocation()) / Math.PI;
        }
        
        // distance of the ball to the centre
        input[index] = (kw.ball.distanceToCenter / (kw.par.size / 2)) * 2 - 1;

        return input;
    }

    @Override
    public void action(double[] output) {
        Keepaway kw = (Keepaway) sim;

        double dashPower = output[0] * kw.par.takerSpeed;
        double dashDir = orientation2D() + (output[1] * Math.PI - Math.PI / 2);
        super.move(dashDir, dashPower);

        // check if it caught the ball
        if (this.distanceTo(kw.ball) == 0) {
            kw.caught = true;
            kw.ball.stop();
        }
    }
}
