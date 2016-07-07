/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.util.Arrays;
import mase.controllers.AgentController;
import mase.mason.world.AbstractEffector;
import mase.mason.world.AbstractSensor;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.RangeBearingSensor;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class EvolvedSoccerAgent extends SoccerAgent {

    private static final long serialVersionUID = 1L;

    public EvolvedSoccerAgent(Soccer sim, AgentController ac, double moveSpeed, double kickSpeed) {
        super(sim, ac, sim.par.agentMoveSpeed, sim.par.agentKickSpeed);
    }

    protected void setupSensors() {
        Soccer soc = (Soccer) super.sim;

        DashMovementEffector eff = new DashMovementEffector(sim, field, this);
        eff.allowBackwardMove(false);
        eff.setSpeeds(moveSpeed, Math.PI);
        super.addEffector(eff);
        KickEffector k = new KickEffector(sim, field, this);
        k.setKickCapabilities(kickSpeed, Math.PI);
        super.addEffector(k);

        DistanceSensorArcs own = new DistanceSensorArcs(sim, field, this);
        own.setObjects(ownTeam);
        own.setArcs(soc.par.sensorArcs);
        own.setRange(Double.POSITIVE_INFINITY);
        super.addSensor(own);

        DistanceSensorArcs opp = new DistanceSensorArcs(sim, field, this);
        opp.setObjects(oppTeam);
        opp.setArcs(soc.par.sensorArcs);
        opp.setRange(Double.POSITIVE_INFINITY);
        super.addSensor(opp);

        RangeBearingSensor rbs = new RangeBearingSensor(sim, field, this);
        rbs.setObjects(Arrays.asList(soc.ball));
        super.addSensor(rbs);

        if (soc.par.goalSensors) {
            RangeBearingSensor rbsGoal = new RangeBearingSensor(sim, field, this);
            rbsGoal.setObjects(Arrays.asList(ownGoal, oppGoal));
            super.addSensor(rbsGoal);
        }

        if (soc.par.possessionSensor) {
            PossessionSensor ps = new PossessionSensor(sim, field, this);
            super.addSensor(ps);
        }

        if (soc.par.locationSensor) {
            LocationSensor ls = new LocationSensor(sim, field, this);
            super.addSensor(ls);
        }
    }

    static class KickEffector extends AbstractEffector {

        double kickSpeed;
        double kickAngle;

        public KickEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
            super(state, field, ag);
        }

        public void setKickCapabilities(double kickSpeed, double kickAngle) {
            this.kickSpeed = kickSpeed;
            this.kickAngle = kickAngle;
        }

        @Override
        public int valueCount() {
            return 2;
        }

        @Override
        public void action(double[] values) {
            double kickDir = ag.orientation2D() + (values[1] * 2 - 1) * kickAngle;
            double kickPower = values[0] * kickSpeed;
            ((SoccerAgent) ag).kickBall(kickDir, kickPower);
        }
    }

    static class LocationSensor extends AbstractSensor {

        public LocationSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
            super(state, field, ag);
        }

        @Override
        public int valueCount() {
            return 2;
        }

        @Override
        public double[] readValues() {
            SoccerAgent sag = (SoccerAgent) ag;
            if (sag.ownGoal.x < sag.oppGoal.x) { // playing left
                return new double[]{ag.getLocation().x, ag.getLocation().y};
            } else { // playing right
                return new double[]{field.width - ag.getLocation().x, field.height - ag.getLocation().y};
            }
        }

        @Override
        public double[] normaliseValues(double[] vals) {
            return new double[]{(vals[0] / field.width) * 2 - 1, (vals[1] / field.height) * 2 - 1};
        }

    }

    static class PossessionSensor extends AbstractSensor {

        public PossessionSensor(SimState state, Continuous2D field, EmboddiedAgent ag) {
            super(state, field, ag);
        }

        @Override
        public int valueCount() {
            return 1;
        }

        @Override
        public double[] readValues() {
            Soccer soc = (Soccer) super.state;
            SoccerAgent sag = (SoccerAgent) this.ag;
            return new double[]{soc.referee.teamPossession == null ? 0 : soc.referee.teamPossession == sag.teamColor ? 1 : -1};
        }

        @Override
        public double[] normaliseValues(double[] vals) {
            return vals;
        }

    }

}
