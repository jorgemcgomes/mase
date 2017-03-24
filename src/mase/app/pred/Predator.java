/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Predator extends SmartAgent {

    public static final double RADIUS = 1.5;
    public static final Color COLOUR = Color.RED;
    private static final long serialVersionUID = 1L;
    protected int captureCount = 0;

    public Predator(PredatorPrey sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        if(sim.par.collisions) {
            this.setCollidableTypes(EmboddiedAgent.class);
        }
        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        double linearSpeed = sim.par.speedsOffset == 0 ? sim.par.predatorLinearSpeed : sim.par.predatorLinearSpeed + sim.par.predatorLinearSpeed * (sim.random.nextDouble() * 2 - 1) * sim.par.speedsOffset;
        double turnSpeed = sim.par.speedsOffset == 0 ? sim.par.predatorTurnSpeed : sim.par.predatorTurnSpeed + sim.par.predatorTurnSpeed * (sim.random.nextDouble() * 2 - 1) * sim.par.speedsOffset;
        dm.setSpeeds(linearSpeed, turnSpeed);
        dm.setNoise(sim.par.speedsNoise, sim.par.speedsNoise);
        super.addEffector(dm);
    }

    void setupSensors() {
        PredatorPrey pp = (PredatorPrey) super.sim;
        // Prey sensor
        if (pp.par.preySensorMode == PredParams.V_ARCS) {
            DistanceSensorArcs ds = new DistanceSensorArcs(sim, field, this);
            ds.setArcs(pp.par.sensorArcs);
            ds.setRange(pp.par.preySensorRange);
            ds.setObjectTypes(Prey.class);
            ds.setNoise(pp.par.rangeNoise, pp.par.orientationNoise, DistanceSensorArcs.UNIFORM);
            super.addSensor(ds);
        } else if (pp.par.preySensorMode == PredParams.V_RBS_CLOSEST || pp.par.preySensorMode == PredParams.V_RBS_ALL) {
            RangeBearingSensor rbs = new RangeBearingSensor(sim, field, this);
            rbs.setObjects(pp.preys);
            rbs.setSort(true);
            if (pp.par.preySensorMode == PredParams.V_RBS_CLOSEST) {
                rbs.setObjectCount(1);
            }
            rbs.setNoise(pp.par.rangeNoise, pp.par.orientationNoise, RangeBearingSensor.UNIFORM);
            super.addSensor(rbs);
        }
        
        // Predator sensor
        if (pp.par.predatorSensorMode == PredParams.V_ARCS) {
            DistanceSensorArcs ds = new DistanceSensorArcs(sim, field, this);
            ds.setArcs(pp.par.sensorArcs);
            ds.setRange(pp.par.predatorSensorRange);
            ds.setObjectTypes(Predator.class);
            ds.setNoise(pp.par.rangeNoise, pp.par.orientationNoise, DistanceSensorArcs.UNIFORM);
            super.addSensor(ds);
        } else if (pp.par.predatorSensorMode == PredParams.V_RBS_CLOSEST || pp.par.predatorSensorMode == PredParams.V_RBS_ALL) {
            RangeBearingSensor rbs = new RangeBearingSensor(sim, field, this);
            rbs.setObjects(pp.predators);
            rbs.setSort(true);
            if (pp.par.predatorSensorMode == PredParams.V_RBS_CLOSEST) {
                rbs.setObjectCount(1);
            }
            rbs.setNoise(pp.par.rangeNoise, pp.par.orientationNoise, RangeBearingSensor.UNIFORM);
            super.addSensor(rbs);
        }
    }

    @Override
    public void action(double[] output) {
        super.action(output);
        // capture preys
        PredatorPrey predSim = (PredatorPrey) sim;
        for (Prey prey : predSim.activePreys) {
            if (this.distanceTo(prey) <= predSim.par.captureDistance) {
                prey.disappear();
                this.captureCount++;
                predSim.captureCount++;
            }
        }
    }

    public int getCaptureCount() {
        return captureCount;
    }
}
