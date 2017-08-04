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
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class PlaygroundSDBC extends MasonEvaluation<VectorBehaviourResult> {

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
        if(!PlaygroundSDBCStandardizer.STORE_FILE.exists()) {
            state.output.warning("Standardization file not found! " + PlaygroundSDBCStandardizer.STORE_FILE.getAbsolutePath());
            return;
        }
        try {
            Pair<double[], double[]> s = PlaygroundSDBCStandardizer.readStandardization(PlaygroundSDBCStandardizer.STORE_FILE);
            state.output.warning("Successfully parsed standardization values: " + PlaygroundSDBCStandardizer.STORE_FILE.getAbsolutePath());
            sMeans = s.getLeft();
            sSDs = s.getRight();
        } catch (Exception ex) {
            ex.printStackTrace();
            state.output.fatal("Error parsing standardization file! " + PlaygroundSDBCStandardizer.STORE_FILE.getAbsolutePath());
        }
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        if(sMeans == null || sSDs == null) {
            System.out.println("Does not have the required standardization factors. Run PlaygroundSDBCStandardizer.");
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
        
        res[5] = pl.agent.getSpeed();
        
        res[6] = pl.agent.getTurningSpeed();
        
        return res;
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        double[] mean = new double[states.get(0).length];
        for(double[] state : states) {
            for(int i = 0 ; i < state.length ; i++) {
                mean[i] += state[i] / states.size();
            }
        }
        
        for(int i = 0 ; i < mean.length; i++) {
            if(sSDs[i] == 0) { // division by zero not cool
                mean[i] = 0;
            } else {
                mean[i] = Math.max(-BOUND, Math.min(BOUND, (mean[i] - sMeans[i]) / sSDs[i]));
            }
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
