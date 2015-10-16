/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class HerdingSimulator extends MasonSimulationProblem {

    private HerdingParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new HerdingParams();
        base = defaultBase();

        par.activeSheep = state.parameters.getBoolean(base.push(HerdingParams.P_ACTIVE_SHEEP), null, false);
        par.agentRadius = state.parameters.getDouble(base.push(HerdingParams.P_AGENT_RADIUS), null);
        par.arenaSize = state.parameters.getDouble(base.push(HerdingParams.P_ARENA_SIZE), null);
        par.discretization = state.parameters.getDouble(base.push(HerdingParams.P_DISCRETIZATION), null);
        par.foxSpeed = state.parameters.getDouble(base.push(HerdingParams.P_FOX_SPEED), null);
        par.foxX = state.parameters.getDouble(base.push(HerdingParams.P_FOX_X), null);
        par.gateSize = state.parameters.getDouble(base.push(HerdingParams.P_GATE_SIZE), null);
        par.herdingRange = state.parameters.getDouble(base.push(HerdingParams.P_HERDING_RANGE), null);
        par.numFoxes = state.parameters.getInt(base.push(HerdingParams.P_NUM_FOXES), null);
        par.numShepherds = state.parameters.getInt(base.push(HerdingParams.P_NUM_SHEPHERDS), null);
        par.sheepSpeed = state.parameters.getDouble(base.push(HerdingParams.P_SHEEP_SPEED), null);
        par.shepherdSensorRange = state.parameters.getDouble(base.push(HerdingParams.P_SHEPHERD_SENSOR_RANGE), null);
        par.shepherdSeparation = state.parameters.getDouble(base.push(HerdingParams.P_SHEPHERD_SEPARATION), null);
        par.shepherdSpeed = state.parameters.getDouble(base.push(HerdingParams.P_SHEPHERD_SPEED), null);
        par.shepherdTurnSpeed = state.parameters.getDouble(base.push(HerdingParams.P_SHEPHERD_TURN_SPEED), null);
        par.randomSheepPosition = state.parameters.getBoolean(base.push(HerdingParams.P_RANDOM_SHEEP_POSITION), null, false);
        par.numSheeps = state.parameters.getInt(base.push(HerdingParams.P_NUM_SHEEPS), null);
        par.sheepX = state.parameters.getDouble(base.push(HerdingParams.P_SHEEP_X), null);
        par.shepherdX = state.parameters.getDouble(base.push(HerdingParams.P_SHEPHERD_X), null);
        par.randomFoxPosition = state.parameters.getBoolean(base.push(HerdingParams.P_RANDOM_FOX_POSITION), null, true);
        par.shepherdArcSensor = state.parameters.getBoolean(base.push(HerdingParams.P_SHEPHERD_ARC_SENSOR), null, true);
        par.smartFox = state.parameters.getBoolean(base.push(HerdingParams.P_SMART_FOX), null, false);
    }

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        return new Herding(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new GUIState2D(createSimState(gc, seed), "Herding", 500, 500, Color.WHITE);
    }

}
