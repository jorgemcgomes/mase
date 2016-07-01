/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import mase.evaluation.FitnessResult;

/**
 *
 * @author jorge
 */
public class MultiRoverFitnessBootstrap extends MultiRoverFitness {
    
    private static final long serialVersionUID = 1L;
    double proximity = 0;
    
    @Override
    protected void evaluate() {
        super.evaluate();
        MultiRover mr = (MultiRover) super.sim;
        if(mr.rocks.isEmpty()) {
            return;
        }
        for(Rover r : mr.rovers) {
            double closestD = Double.POSITIVE_INFINITY;
            for(Rock rock : mr.rocks) {
                double d = r.getLocation().distance(rock.getLocation());
                if(d < closestD) {
                    closestD = d;
                }
            }
            proximity += closestD;
        }
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        MultiRover mr = (MultiRover) super.sim;
        double bootstrap = 0.01 - proximity / mr.rovers.size() / currentEvaluationStep / mr.field.width / 100d;
        this.fitnessResult = new FitnessResult(fitnessResult.value() + bootstrap, fitnessResult.getAverageType());
    }
    
    
    
}
