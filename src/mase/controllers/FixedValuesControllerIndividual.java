/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.vector.DoubleVectorIndividual;

/**
 *
 * @author jorge
 */
public class FixedValuesControllerIndividual extends DoubleVectorIndividual implements AgentControllerIndividual {

    private static final long serialVersionUID = 1L;

    @Override
    public AgentController decodeController() {
        return new FixedValuesController(genome);
    }
}
