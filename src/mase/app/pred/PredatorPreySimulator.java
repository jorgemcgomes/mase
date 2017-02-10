/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import sim.display.GUIState;

/**
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredatorPreySimulator extends MasonSimulationProblem<PredatorPrey> {

    private static final long serialVersionUID = 1L;
    private PredParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new PredParams();
        ParamUtils.autoSetParameters(par, state, base, super.defaultBase(), true);


        if (par.escapeStrategy != PredParams.V_NEAREST && par.escapeStrategy != PredParams.V_MEAN_VECTOR) {
            state.output.fatal("Unknown escape strategy: " + par.escapeStrategy);
        }

        if (par.preySensorMode == PredParams.V_ARCS || par.predatorSensorMode == PredParams.V_ARCS) {
            if(par.sensorArcs % 2 != 0) {
                state.output.fatal("The number of sensor arcs must be even: " + par.sensorArcs);
            }
        }
    }

    @Override
    public PredatorPrey createSimState(GroupController gc, long seed) {
        return new PredatorPrey(seed, par, gc);
    }
}