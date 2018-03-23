/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

/**
 *
 * @author Jorge
 */
public class AntiPhototaxisFitness extends PhototaxisFitness {
    
    private static final long serialVersionUID = 1L;

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        return 1 - super.getFinalTaskFitness(sim);
    }
}
