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
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import sim.display.GUIState;

/**
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredatorPreySimulator extends MasonSimulationProblem {

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
        return new GUIState2D(createSimState(cs, seed), "Predator-prey", 500, 500, Color.WHITE);
    }
}