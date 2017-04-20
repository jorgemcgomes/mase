/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.evaluation.FitnessResult;

/**
 *
 * @author jorge
 */
public class ProximityFitness extends AvoidanceFitness {

    @Override
    public FitnessResult getResult() {
        return new FitnessResult(1 - super.getResult().value());
    }
    
}
