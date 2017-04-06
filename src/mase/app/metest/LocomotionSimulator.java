/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.metest;

import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;

/**
 *
 * @author jorge
 */
public class LocomotionSimulator extends MasonSimulationProblem<LocomotionTask> {

    @Override
    protected LocomotionTask createSimState(GroupController gc, long seed) {
        return new LocomotionTask(gc, seed);
    }


}
