/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Keeper extends SmartAgent {

    public static final double RADIUS = 2;
    public static final double KICK_DISTANCE = 0.5;
    protected boolean hasPossession = false;
    protected double passSpeed;
    protected double moveSpeed;
    private boolean justKicked = false;

    public Keeper(Keepaway sim, Continuous2D field, AgentController ac, double passSpeed, double moveSpeed, Color color) {
        super(sim, field, RADIUS, color, ac);
        this.passSpeed = passSpeed;
        this.moveSpeed = moveSpeed;

        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(moveSpeed, Math.PI / 4);
        super.addEffector(dm);
    }

    protected void setupSensors() {
        Keepaway kw = (Keepaway) sim;
        RangeBearingSensor keepersRBS = new RangeBearingSensor();
        keepersRBS.setObjects(kw.keepers);
        keepersRBS.setSort(kw.par.sortKeepers);
        super.addSensor(keepersRBS);

        RangeBearingSensor otherRBS = new RangeBearingSensor();
        ArrayList<Object> list = new ArrayList<Object>(kw.takers);
        list.add(kw.ball);
        otherRBS.setObjects(list);
        otherRBS.setSort(false);
        super.addSensor(otherRBS);
    }

    @Override
    public void action(double[] output) {
        Keepaway kw = (Keepaway) sim;
        if (justKicked
                && (kw.ball.getSpeed() < 0.0001
                || kw.ball.getLocation().distance(getLocation()) > Keeper.RADIUS + Ball.RADIUS + KICK_DISTANCE)) {
            justKicked = false;
        }
        if (!justKicked && kw.ball.distanceTo(this) < KICK_DISTANCE) {
            this.hasPossession = true;
            justKicked = true;
            double kickPower = output[2] * passSpeed;
            double kickDir = orientation2D() + (output[3] * Math.PI * 2 - Math.PI);
            kw.ball.kick(kickDir, kickPower);
        } else {
            this.hasPossession = false;
            super.action(output);
        }
    }

    @Override
    public double[] getStateVariables() {
        double[] agVars = super.getStateVariables();
        double[] newVars = Arrays.copyOf(agVars, agVars.length + 1);
        newVars[newVars.length - 1] = hasPossession || justKicked ? 1 : 0;
        return newVars;
    }
}
