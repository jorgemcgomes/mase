/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.evaluation.VectorBehaviourResult;
import mase.evaluation.VectorBehaviourResult.LocationEstimator;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SwarmSDBC extends MasonEvaluation<VectorBehaviourResult> {

    private static final long serialVersionUID = 1L;
    private static final double BOUND = 3;
    private VectorBehaviourResult vbr;
    private List<double[]> states;
    private double[] sMeans;
    private double[] sSDs;
    public static final String P_AGGREGATION = "aggregation";
    public static final String P_SD = "sd";
    public static final String P_STD = "standardization";
    private File standardizationFile;
    private LocationEstimator agg;
    private boolean sd = false;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        agg = VectorBehaviourResult.LocationEstimator.valueOf(state.parameters.getString(base.push(P_AGGREGATION), null));
        sd = state.parameters.getBoolean(base.push(P_SD), null, false);
        standardizationFile = state.parameters.getFile(base.push(P_STD), null);

        if(standardizationFile == null) {
            state.output.warning("Standardization file not specified!", base.push(P_STD));
            return;
        }
        if(!standardizationFile.exists()) {
            state.output.warning("Standardization file not found! " + standardizationFile.getAbsolutePath());
            return;
        }
        try {
            Pair<double[], double[]> s = SwarmSDBCStandardizer.readStandardization(standardizationFile);
            state.output.warning("Successfully parsed standardization values: " + standardizationFile.getAbsolutePath());
            sMeans = s.getLeft();
            sSDs = s.getRight();
        } catch (Exception ex) {
            ex.printStackTrace();
            state.output.fatal("Error parsing standardization file! " + standardizationFile.getAbsolutePath());
        }
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        if(sMeans == null || sSDs == null) {
            System.out.println("Does not have the required standardization factors. Run SwarmSDBCStandardizer.");
            System.exit(1);
        }
        
        states = new ArrayList<>(maxEvaluationSteps+1);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        states.add(state((SwarmPlayground) sim));
    }
    
    // agent-to-agents mean distance; agent-to-closest-agent distance
    // agent-to-obstacles mean distance; agent-to-closest-obstacle distance
    // agent-to-pois mean distance; agent-to-closest-pois mean distance
    // agent linear speed; agent turn speed 
    protected double[] state(SwarmPlayground pl) {
        double[] res = new double[8];        
        for(SwarmAgent sa : pl.agents) {
            double md = 0;
            double min = Double.POSITIVE_INFINITY;
            // distances to other agents
            for(SwarmAgent other : pl.agents) {
                if(sa != other) {
                    double d = sa.distanceTo(other);
                    md += d;
                    min = Math.min(min, d);
                }
            }
            res[0] += md / (pl.agents.size() - 1);
            res[1] += min;
            
            // distances to obstacles or walls
            md = sa.distanceTo(pl.walls);
            min = md;
            for (MultilineObject o : pl.obstacles) {
                double d = sa.distanceTo(o);
                md += d;
                min = Math.min(d, min);
            }
            res[2] += md / (pl.obstacles.size() + 1);
            if(pl.par.maxObstacles == 0) { // the min does not make sense if there's only one (the walls)... mean=min
                res[3] = Double.NaN;
            } else {
                res[3] += min;
            }

            // distances to POIs
            if(pl.par.maxObjects > 0) {
                md = 0;
                min = Double.POSITIVE_INFINITY;
                for(CircularObject o : pl.objects) {
                    double d = sa.distanceTo(o);
                    md += d;
                    min = Math.min(d, min);
                }
                res[4] += md / pl.objects.size();
                if(pl.par.maxObjects == 1) { // the min doesnt make sense
                    res[5] = Double.NaN;
                } else {
                    res[5] += min;
                }
            } else {
                res[4] = Double.NaN;
                res[5] = Double.NaN;
            }

            res[6] += sa.getSpeed();
            res[7] += sa.getTurningSpeed();            
        }  
        
        for(int i = 0 ; i < res.length ; i++) {
            res[i] /= pl.agents.size();
        }
        
        return res;
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        DescriptiveStatistics[] ds = new DescriptiveStatistics[states.get(0).length];
        for(int i = 0 ; i < ds.length ; i++) {
            ds[i] = new DescriptiveStatistics();
        }
        for (double[] state : states) {
            for (int i = 0; i < state.length; i++) {
                if(!Double.isNaN(state[i])) {
                    ds[i].addValue(state[i]);
                }
            }
        }

        double[] bc = new double[sd ? ds.length * 2 : ds.length];
        for (int i = 0; i < ds.length; i++) {
            if (sSDs[i] == 0 || ds[i].getN() == 0) { // division by zero not cool
                bc[i] = 0;
            } else {
                bc[i] = Math.max(-BOUND, Math.min(BOUND, (ds[i].getMean() - sMeans[i]) / sSDs[i]));
            }
        }
        if(sd) {
            for (int i = 0; i < ds.length; i++) {
                if (sSDs[i] == 0 || ds[i].getN() == 0) { // nothing to see here
                    bc[ds.length + i] = 0;
                } else {
                    bc[ds.length + i] = Math.max(-BOUND, Math.min(BOUND, (ds[i].getStandardDeviation() / sSDs[i]) * 2 - 1));
                }
            }
        }
        vbr = new VectorBehaviourResult(bc);
        vbr.setDistance(VectorBehaviourResult.Distance.euclidean);
        vbr.setLocationEstimator(agg);
    }
    
    @Override
    public VectorBehaviourResult getResult() {
        return vbr;
    }
    
}
