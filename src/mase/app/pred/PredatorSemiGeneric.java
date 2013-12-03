/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.pred;

import mase.generic.SemiGenericEvaluator;

/**
 *
 * @author jorge
 */
public class PredatorSemiGeneric extends SemiGenericEvaluator {

    @Override
    protected void preSimulation() {
        super.preSimulation();
        PredatorPrey pred = (PredatorPrey) sim;
        Predator[] predators = new Predator[pred.predators.size()];
        pred.predators.toArray(predators);
        super.addAgentGroup(predators);
        Prey[] preys = new Prey[pred.preys.size()];
        pred.preys.toArray(preys);
        super.addAgentGroup(preys);
    }    
}
