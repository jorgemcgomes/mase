/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;

/**
 *
 * @author jorge
 */
public class PlaygroundSDBCRaw extends MasonEvaluation<VectorBehaviourResult> {

    private static final long serialVersionUID = 1L;
    private static final double BOUND = 2;
    private VectorBehaviourResult vbr;
    private List<double[]> states;
    private double[] sMeans;
    private double[] sSDs;

    protected void setStandardizationScores(double[] means, double[] sds) {
        this.sMeans = means;
        this.sSDs = sds;
        for(int i = 0 ; i < sSDs.length ; i++) {
            if(sSDs[i] == 0) {
                sSDs[i] = 1;
            }
        }
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        states = new ArrayList<>(maxEvaluationSteps+1);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        states.add(state((Playground) sim));
    }
    
    // agent-to-walls distance ; agent-to-obstacles mean distance; agent-to-objects mean distance; agent linear speed; agent turn speed 
    // TODO: justifiable way to normalise: generate a lot of random environments, measure the min/mean/max for each distance and normalise based on that
    protected double[] state(Playground pl) {
        double[] res = new double[5];
        res[0] = pl.agent.distanceTo(pl.walls);
        
        if(!pl.obstacles.isEmpty()) {
            double md = 0;
            for(MultilineObject o : pl.obstacles) {
                md += pl.agent.distanceTo(o);
            }
            res[1] = md / pl.obstacles.size();
        }
        
        if(!pl.objects.isEmpty()) {
            double md = 0;
            for(CircularObject o : pl.objects) {
                md += pl.agent.distanceTo(o);
            }
            res[2] = md / pl.objects.size();
        }
        
        res[3] = pl.par.backMove ? pl.agent.getSpeed() / pl.par.linearSpeed : (pl.agent.getSpeed() / pl.par.linearSpeed) * 2 - 1;
        
        res[4] = pl.agent.getTurningSpeed() / pl.par.turnSpeed;
        
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
        
        mean[0] = Math.max(-BOUND, Math.min(BOUND, (mean[0] - sMeans[0]) / sSDs[0]));
        mean[1] = Math.max(-BOUND, Math.min(BOUND, (mean[1] - sMeans[1]) / sSDs[1]));
        mean[2] = Math.max(-BOUND, Math.min(BOUND, (mean[2] - sMeans[2]) / sSDs[2]));
        //mean[3] = mean[3] * BOUND;
        //mean[4] = mean[4] * BOUND;
        
        vbr = new VectorBehaviourResult(mean);
        vbr.setDistance(VectorBehaviourResult.EUCLIDEAN);
        vbr.setLocationEstimator(VectorBehaviourResult.GEOMETRIC_MEDIAN);
    }
    
    @Override
    public VectorBehaviourResult getResult() {
        return vbr;
    }
    
}
