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
import mase.evaluation.VectorBehaviourResult.LocationEstimator;
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
    private static final double BOUND = 3;
    private VectorBehaviourResult vbr;
    private List<double[]> states;
    private double[] sMeans;
    private double[] sSDs;
    public static final String P_AGGREGATION = "aggregation";
    private LocationEstimator agg;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        agg = VectorBehaviourResult.LocationEstimator.valueOf(state.parameters.getString(base.push(P_AGGREGATION), null));
    }

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
        
        if(sMeans == null || sSDs == null) {
            System.out.println("Does not have the required standardization factors. See PlaygroundSDBCStandardizer.");
            System.exit(1);
        }
        
        states = new ArrayList<>(maxEvaluationSteps+1);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        states.add(state((Playground) sim));
    }
    
    // agent-to-walls distance ; agent-to-obstacles mean distance; agent-to-closest-obstacle distance; agent-to-objects mean distance; agent-to-closest-object mean distance; agent linear speed; agent turn speed 
    // TODO: justifiable way to normalise: generate a lot of random environments, measure the min/mean/max for each distance and normalise based on that
    protected double[] state(Playground pl) {
        double[] res = new double[7];
        res[0] = pl.agent.distanceTo(pl.walls);
        
        if(!pl.obstacles.isEmpty()) {
            double md = 0;
            double min = Double.POSITIVE_INFINITY;
            for(MultilineObject o : pl.obstacles) {
                double d = pl.agent.distanceTo(o);
                md += d;
                min = Math.min(d, min);
            }
            res[1] = md / pl.obstacles.size();
            res[2] = min;
        }
        
        if(!pl.objects.isEmpty()) {
            double md = 0;
            double min = Double.POSITIVE_INFINITY;
            for(CircularObject o : pl.objects) {
                double d = pl.agent.distanceTo(o);
                md += d;
                min = Math.min(d, min);
            }
            res[3] = md / pl.objects.size();
            res[4] = min;
        }
        
        res[5] = pl.par.backMove ? pl.agent.getSpeed() / pl.par.linearSpeed : (pl.agent.getSpeed() / pl.par.linearSpeed) * 2 - 1;
        
        res[6] = pl.agent.getTurningSpeed() / pl.par.turnSpeed;
        
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
        
        // the last 2 are assumed to be already normalised (agent turning and linear speed)
        for(int i = 0 ; i < mean.length - 2 ; i++) {
            mean[i] = Math.max(-BOUND, Math.min(BOUND, (mean[i] - sMeans[i]) / sSDs[i]));
        }
        
        vbr = new VectorBehaviourResult(mean);
        vbr.setDistance(VectorBehaviourResult.Distance.euclidean);
        vbr.setLocationEstimator(agg);
    }
    
    @Override
    public VectorBehaviourResult getResult() {
        return vbr;
    }
    
}
