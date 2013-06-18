/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class SimulationProblem extends Problem implements GroupedProblemForm {

    public static final String P_DECODER = "group-controller";
    public static final String P_SIMULATOR = "sim";
    protected ControllerDecoder decoder;
    protected Simulator sim;
    public static final String P_TRIALS_MERGE = "trials-merge";
    public static final String V_BEST = "best", V_MEAN = "mean", V_MEDIAN = "median";
    public static final int MERGE_BEST = 0, MERGE_MEAN = 1, MERGE_MEDIAN = 2;
    protected int mergeMode;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        sim = (Simulator) state.parameters.getInstanceForParameter(base.push(P_SIMULATOR), null, Simulator.class);
        sim.setup(state, base.push(P_SIMULATOR));

        decoder = (ControllerDecoder) state.parameters.getInstanceForParameter(base.push(P_DECODER), null, ControllerDecoder.class);
        decoder.setup(state, base.push(P_DECODER));

        if (!state.parameters.exists(base.push(P_TRIALS_MERGE), null)) {
            mergeMode = MERGE_BEST;
            state.output.warning("Parameter not found. Going with mean.", base.push(P_TRIALS_MERGE));
        } else {
            String val = state.parameters.getString(base.push(P_TRIALS_MERGE), null);
            if (val.equals(V_BEST)) {
                mergeMode = MERGE_BEST;
            } else if (val.equals(V_MEAN)) {
                mergeMode = MERGE_MEAN;
            } else if (val.equals(V_MEDIAN)) {
                mergeMode = MERGE_MEDIAN;
            } else {
                state.output.fatal("Unknown parameter value.", base.push(P_TRIALS_MERGE));
            }
        }
    }

    @Override
    public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment, boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                pop.subpops[i].individuals[j].fitness.trials = new ArrayList();
            }
        }
    }

    @Override
    public void postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                Individual ind = pop.subpops[i].individuals[j];
                ExpandedFitness[] trials = Arrays.copyOf(ind.fitness.trials.toArray(), ind.fitness.trials.size(), ExpandedFitness[].class);
                for (int k = 0; k < trials.length; k++) {
                    trials[k] = (ExpandedFitness) ind.fitness.trials.get(k);
                }
                switch (mergeMode) {
                    case MERGE_MEAN:
                        ind.fitness.setToMeanOf(state, trials);
                        break;
                    case MERGE_MEDIAN:
                        ind.fitness.setToMedianOf(state, trials);
                        break;
                    case MERGE_BEST:
                        ind.fitness.setToBestOf(state, trials);
                }
                ind.evaluated = true;
                ((ExpandedFitness) ind.fitness).setCorrespondingSubpop(i);
            }
        }
    }

    @Override
    public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        /* Perform simulation */
        GroupController controller = decoder.decodeController(ind);
        EvaluationResult[] eval = sim.evaluateSolution(controller, state.random[threadnum].nextLong());

        /* Save results */
        for (int i = 0; i < ind.length; i++) {
            if (updateFitness[i]) {
                ExpandedFitness trial = (ExpandedFitness) ind[i].fitness.clone();
                trial.setEvaluations(eval);
                trial.setFitness(state, trial.fitnessScore(), false);
                trial.setContext(ind);
                ind[i].fitness.trials.add(trial);
            }
        }
    }

    public ControllerDecoder getControllerDecoder() {
        return decoder;
    }

    public Simulator getSimulator() {
        return sim;
    }
}
