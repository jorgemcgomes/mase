/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import mase.mason.MasonSimState;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class AggregationFitness extends SwarmFitness {

    private static final long serialVersionUID = 1L;
    protected double sum;
    protected MutableDouble2D currentCM;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        sum = 0;
        currentCM = new MutableDouble2D();
    }
    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        SwarmPlayground sw = (SwarmPlayground) sim;
        currentCM.setTo(0, 0);
        for (SwarmAgent aa : sw.agents) {
            currentCM.addIn(aa.getLocation());
        }
        currentCM.multiplyIn(1.0 / sw.agents.size());
        double dist = 0;
        for (SwarmAgent aa : sw.agents) {
            dist += currentCM.distance(aa.getLocation());
        }
        sum += dist / sw.agents.size();        
    }
    

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        return Math.max(0, 1 - sum / currentEvaluationStep / (sim.par.arenaSize / 2));
    }
}
