/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.world.AbstractSensor;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class Rover extends SmartAgent {

    public static final double RADIUS = 1.5;
    public static final Color COLOUR = Color.BLACK;
    public static final int LOW = -1, NONE = 0, HIGH = 1;

    protected int actuatorType;
    protected long lastActivation = -10000;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        this.enableCollisionDetection(true);
        this.enableBoundedArena(true);

        // redrock sensor
        DistanceSensorArcs ds = new DistanceSensorArcs();
        ds.setArcs(sim.par.sensorArcs);
        ds.setRange(sim.par.sensorRange);
        ds.setObjectTypes(RedRock.class);
        super.addSensor(ds);

        // closest rovers distance
        DistanceSensorArcs dsr = new DistanceSensorArcs();
        dsr.setArcs(sim.par.sensorArcs);
        dsr.setRange(sim.par.sensorRange);
        dsr.setObjectTypes(Rover.class);
        super.addSensor(dsr);

        // closest rovers type
        TypeSensor ts = new TypeSensor(dsr);
        super.addSensor(ts);

        // movement effector
        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.speed, sim.par.rotationSpeed);
        super.addEffector(dm);

    }

    private static class TypeSensor extends AbstractSensor {

        DistanceSensorArcs ds;

        TypeSensor(DistanceSensorArcs ds) {
            this.ds = ds;
        }

        @Override
        public int valueCount() {
            return ds.valueCount();
        }

        @Override
        public double[] readValues() {
            Object[] rovers = ds.getClosestObjects();
            double[] vals = new double[rovers.length];
            for (int i = 0; i < rovers.length; i++) {
                if (rovers[i] == null) {
                    vals[i] = 0;
                } else {
                    vals[i] = ((Rover) rovers[i]).getActuatorType();
                }
            }
            return vals;
        }

        @Override
        public double[] normaliseValues(double[] vals) {
            return vals;
        }

    }

    public int getActuatorType() {
        return actuatorType;
    }

    @Override
    public void action(double[] output) {
        if (sim.schedule.getSteps() - lastActivation > ((MultiRover) sim).par.minActivationTime) {
            if (output[2] > 0.5 || output[3] > 0.5) {
                actuatorType = output[2] >= output[3] ? -1 : 1;
                lastActivation = sim.schedule.getSteps();
            } else {
                actuatorType = 0;
            }
        }
        super.action(output);
    }

}
