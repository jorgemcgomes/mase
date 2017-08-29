/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.text.DecimalFormat;
import mase.MaseProblem;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ConsoleStat extends Statistics {

    private static final long serialVersionUID = 1L;
    int jobs;
    int currentJob;
    DecimalFormat nf;
    double bestFitness = Double.NEGATIVE_INFINITY;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        jobs = state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        currentJob = state.job != null ? (Integer) state.job[0] : 0;
        nf = new DecimalFormat("0.0000");
        state.output.setStore(false);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        // print best fitness and min / 1st quartile / median / 3rd quartile / max per subpop
        double maxGen = Double.NEGATIVE_INFINITY;
        for (int s = 0; s < state.population.subpops.size(); s++) {
            DescriptiveStatistics ds = new DescriptiveStatistics(state.population.subpops.get(s).individuals.size());
            for (Individual ind : state.population.subpops.get(s).individuals) {
                //System.out.println(((ExpandedFitness) ind.fitness).getFitnessScore());
                ds.addValue(((ExpandedFitness) ind.fitness).getFitnessScore());
            }
            state.output.message("Subpop " + s + 
                    " Mean: " + nf.format(ds.getMean()) + 
                    " | Min: " + nf.format(ds.getMin()) + 
                    " | Q1: "  + nf.format(ds.getPercentile(25)) + 
                    " | Q2: " + nf.format(ds.getPercentile(50)) + 
                    " | Q3: "+ nf.format(ds.getPercentile(75)) + 
                    " | Max:" + nf.format(ds.getMax()));
            if (ds.getMax() > maxGen) {
                maxGen = ds.getMax();
            }
            if (ds.getMax() > bestFitness) {
                bestFitness = ds.getMax();
            }
        }
        // TODO: so much hacking, just to obtain the output folder :/
        RunStatistics stat = (RunStatistics) state.statistics.children[7];
        String ip = RunStatistics.getComputerName();
        state.output.message("Exp: " + stat.file.getParent() + " Job: " + state.job[0] + " IP: " + ip);
        String status;
        if(state.numEvaluations > 0) {
            int done = state.evaluations;
            int limit = (int) state.numEvaluations;
            status = "Evals: " + done / 1000 + "/" + limit / 1000+"k";
        } else {
            status = state.generation + "/" + state.numGenerations;
        }        
        state.output.message(status + " | Best gen: " + nf.format(maxGen) + " | "
                + "Best so far: " + nf.format(bestFitness));
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        state.output.message("Best fitness: " + nf.format(bestFitness));
    }
}
