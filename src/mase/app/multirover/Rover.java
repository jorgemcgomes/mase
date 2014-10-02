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
import mase.mason.world.DistanceSensorRays;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class Rover extends SmartAgent {

    public static final double RADIUS = 2;
    public static final Color COLOUR = Color.BLACK;
    public static final int LOW = -1, NONE = 0, HIGH = 1;

    protected int actuatorType;
    protected long lastActivation = -10000;
    protected int numActivations = 0;
    protected int captured = 0;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        this.enableCollisionDetection(true);
        this.enableBoundedArena(true);

        DistanceSensorRays dw = new DistanceSensorRays();
        dw.setAgent(sim, field, this);
        dw.setRays(5.0, -Math.PI / 6, Math.PI / 6);
        dw.setBinary(true);
        super.addSensor(dw);

        // redrock sensor
        //TypeDistanceSensor ds = new TypeDistanceSensor();
        DistanceSensorArcs ds = new DistanceSensorArcs();
        ds.setArcs(new double[]{-Math.PI / 2, -Math.PI / 6, Math.PI / 6}, new double[]{-Math.PI / 6, Math.PI / 6, Math.PI / 2});
        ds.setRange(sim.par.sensorRange);
        ds.setObjectTypes(RedRock.class);
        super.addSensor(ds);

        // closest rovers distance
        DistanceSensorArcs dsr = new DistanceSensorArcs();
        dsr.setArcs(new double[]{-Math.PI / 2, -Math.PI / 6, Math.PI / 6}, new double[]{-Math.PI / 6, Math.PI / 6, Math.PI / 2});
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
            return 1;
        }

        @Override
        /*
        WARNING: ONLY WORKS FOR 2-Agent setups!!!
        */
        public double[] readValues() {
            Object[] rovers = ds.getClosestObjects();
            
            int closestType = 0;
            for (int i = 0; i < rovers.length; i++) {
                if (rovers[i] != null) {
                    closestType = ((Rover) rovers[i]).getActuatorType();
                }
            }
            return new double[]{closestType};
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
                int newType = output[2] > output[3]? LOW : HIGH;
                if (actuatorType != newType) {
                    lastActivation = sim.schedule.getSteps();
                    actuatorType = newType;
                    numActivations++;
                }
            } else {
                actuatorType = NONE;
                super.action(output);
            }
        }
    }

}
