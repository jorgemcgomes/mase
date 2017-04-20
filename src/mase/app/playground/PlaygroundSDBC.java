/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class PlaygroundSDBC extends MasonEvaluation<VectorBehaviourResult> {

    public static final String P_INCLUDE_LAST = "include-last";
    private static final long serialVersionUID = 1L;
    private boolean includeLast;
    private VectorBehaviourResult vbr;
    private List<double[]> states;
    private double distNorm;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        includeLast = state.parameters.getBoolean(base.push(P_INCLUDE_LAST),null, true);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        states = new ArrayList<>(maxEvaluationSteps+1);
        distNorm = ((Playground) sim).par.arenaSize / 2;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        states.add(state((Playground) sim));
    }
    
    // agent-to-walls distance ; agent-to-obstacles mean distance; agent-to-objects mean distance; agent linear speed; agent turn speed 
    protected double[] state(Playground pl) {
        double[] res = new double[5];
        res[0] = pl.agent.distanceTo(pl.walls) / distNorm;
        
        double md = 0;
        for(MultilineObject o : pl.obstacles) {
            md += pl.agent.distanceTo(o);
        }
        res[1] = md / pl.obstacles.size() / distNorm;
        
        md = 0;
        for(CircularObject o : pl.objects) {
            md += pl.agent.distanceTo(o);
        }
        res[2] = md / pl.objects.size() / distNorm;
        
        res[3] = pl.par.backMove ? (pl.agent.getSpeed() / pl.par.linearSpeed + 1) / 2 : pl.agent.getSpeed() / pl.par.linearSpeed;
        
        res[4] = (pl.agent.getTurningSpeed() / pl.par.turnSpeed + 1) / 2;
        
        return res;
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        double[] mean = new double[states.get(0).length];
        for(double[] state : states) {
            for(int i = 0 ; i < mean.length ; i++) {
                mean[i] += state[i] / states.size();
            }
        }
        
        if(includeLast) {
            vbr = new VectorBehaviourResult(ArrayUtils.addAll(mean, states.get(states.size() - 1)));
        } else {
            vbr = new VectorBehaviourResult(mean);
        }
    }
    
    @Override
    public VectorBehaviourResult getResult() {
        return vbr;
    }
    
}
