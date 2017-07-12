/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.Statistics;
import mase.controllers.HomogeneousGroupController;
import mase.evaluation.EvaluationFunction;
import mase.mason.MasonSimulationProblem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class PlaygroundSDBCStandardizer extends Statistics {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        
        MasonSimulationProblem prob = (MasonSimulationProblem) state.evaluator.p_problem;
        PlaygroundSDBCRaw fun = null;
        for (EvaluationFunction ef : prob.getEvalFunctions()) {
            if (ef instanceof PlaygroundSDBCRaw) {
                fun = (PlaygroundSDBCRaw) ef;
                break;
            }
        }
        
        if(fun == null) {
            state.output.warning("PlaygroundSDBCRaw evaluation function not found. Standardizer not run.");
            return;
        }
        
        DescriptiveStatistics[] ds = null;
        for (int i = 0; i < 1000; i++) {
            Playground pl = (Playground) prob.getSimState(new HomogeneousGroupController(null), i);
            pl.par.randomPosition = true;
            pl.start();
            double[] s = fun.state(pl);
            
            if (ds == null) {
                ds = new DescriptiveStatistics[s.length];
                for (int j = 0; j < s.length; j++) {
                    ds[j] = new DescriptiveStatistics();
                }
            }
            
            for (int j = 0; j < s.length; j++) {
                ds[j].addValue(s[j]);
            }            
        }
        
        double[] means = new double[ds.length];
        double[] sds = new double[ds.length];
        for(int i = 0 ; i < ds.length ; i++) {
            means[i] = ds[i].getMean();
            sds[i] = ds[i].getStandardDeviation();
            state.output.message("Feature " + i + ": Mean: " + ds[i].getMean() + " SD: " + ds[i].getStandardDeviation() + " Min: " + ds[i].getMin() + " Max: " + ds[i].getMax());
        }
        
        fun.setStandardizationScores(means, sds);
        
    }
                 //double v = (vbr.getOriginalResult()[i] - means[i]) / sds[i];
                //vbr.getBehaviour()[i] = (float) Math.max(-BOUND, Math.min(BOUND, v));   
}
