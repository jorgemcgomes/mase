/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.app.pred.PredParams.SensorMode;
import mase.controllers.GroupController;
import mase.mason.Mason2dUI;
import mase.mason.MasonSimulator;
import sim.display.GUIState;

/**
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredatorPreySimulator extends MasonSimulator {

    private PredParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new PredParams();
        /* Mandatory parameters */
        base = defaultBase();
        par.size = state.parameters.getDouble(base.push(PredParams.P_SIZE), null);
        par.discretization = state.parameters.getDouble(base.push(PredParams.P_DISCRETIZATION), null);
        par.preySpeed = state.parameters.getDouble(base.push(PredParams.P_PREY_SPEED), null);
        par.escapeDistance = state.parameters.getDouble(base.push(PredParams.P_ESCAPE_DISTANCE), null);
        par.predatorSpeed = state.parameters.getDouble(base.push(PredParams.P_PREDATOR_SPEED), null);
        par.captureDistance = state.parameters.getDouble(base.push(PredParams.P_CAPTURE_DISTANCE), null);
        par.predatorSeparation = state.parameters.getDouble(base.push(PredParams.P_PREDATOR_SEPARATION), null);
        par.preyPlacement = state.parameters.getString(base.push(PredParams.P_PREY_PLACEMENT), null);
        par.nPreys = state.parameters.getInt(base.push(PredParams.P_NPREYS), null);
        par.nPredators = state.parameters.getInt(base.push(PredParams.P_NPREDATORS), null);
        par.predatorRotateSpeed = state.parameters.getDouble(base.push(PredParams.P_PREDATOR_ROTATE_SPEED), null);
        par.escapeStrategy = state.parameters.getString(base.push(PredParams.P_ESCAPE_STRATEGY), null);
        par.collisions = state.parameters.getBoolean(base.push(PredParams.P_COLLISIONS), null, false);

        if (par.escapeStrategy.equals(PredParams.V_MEAN_VECTOR)) {
            par.escapeStrategy = PredParams.V_MEAN_VECTOR;
        } else if (par.escapeStrategy.equals(PredParams.V_NEAREST)) {
            par.escapeStrategy = PredParams.V_NEAREST;
        } else {
            state.output.fatal("Unknown strategy: " + par.escapeStrategy, base.push(PredParams.P_ESCAPE_STRATEGY));
        }

        par.sensorMode = SensorMode.valueOf(state.parameters.getString(base.push(PredParams.P_SENSOR_MODE), null));
        if (par.sensorMode == SensorMode.arcs) {
            par.sensorArcs = state.parameters.getInt(base.push(PredParams.P_SENSOR_ARCS), null);
            if(par.sensorArcs % 2 != 0) {
                state.output.fatal("The number of sensor arcs must be even.", base.push(PredParams.P_SENSOR_ARCS));
            }
        }

        /* Optional parameters - only used with some options */
        if (state.parameters.exists(base.push(PredParams.P_PREY_MARGIN))) {
            par.preyMargin = state.parameters.getDouble(base.push(PredParams.P_PREY_MARGIN), null);
        }
        if (state.parameters.exists(base.push(PredParams.P_PREY_SEPARATION))) {
            par.preySeparation = state.parameters.getDouble(base.push(PredParams.P_PREY_SEPARATION), null);
        }
    }

    @Override
    public PredatorPrey createSimState(GroupController gc, long seed) {
        return new PredatorPrey(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController cs, long seed) {
        return new Mason2dUI(createSimState(cs, seed), "Predator-prey", 500, 500, Color.WHITE);
    }
}


/*public static void main(String[] args) {
 // SHOW
 /*PredatorPreySimulator sim = new PredatorPreySimulator();
 GUIState gui = sim.createSimStateWithUI(null, System.nanoTime());
 gui.createController();*/
// BENCHMARK
        /*final int REPS = 1000, MAX_STEPS = 500;
 PredatorPreySimulator sim = new PredatorPreySimulator();
 double avgInit = 0;
 double avgSim = 0;
 double avgSteps = 0;
 for (int i = 0; i < REPS; i++) {
 long t1 = System.currentTimeMillis();
 SimState state = sim.createSimState(null, System.nanoTime());
 state.start();
 long t2 = System.currentTimeMillis();
 while (state.schedule.getSteps() < MAX_STEPS && state.schedule.step(state)) {
 ;
 }
 long steps = state.schedule.getSteps();
 state.finish();
 long t3 = System.currentTimeMillis();
 avgInit += (t2 - t1);
 avgSim += (t3 - t2);
 avgSteps += steps;
 }
 avgInit /= REPS;
 avgSim /= REPS;
 avgSteps /= REPS;
 double avgSimStep = avgSim / avgSteps;
 System.out.println("Avg initialization time: " + avgInit);
 System.out.println("Avg simulation time    : " + avgSim);
 System.out.println("Avg simulation steps   : " + avgSteps);
 System.out.println("Avg time per step      : " + avgSimStep);*/
/*
 2 Preys
 Avg initialization time: 0.0274
 Avg simulation time    : 0.3077
 Avg simulation steps   : 92.6456
 Avg time per step      : 0.003321258645850423
 20 preys
 Avg initialization time: 0.0384
 Avg simulation time    : 3.2094
 Avg simulation steps   : 471.6097
 Avg time per step      : 0.006805203540130748
         
 }*/
