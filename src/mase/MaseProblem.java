/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import mase.controllers.ControllerFactory;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.io.Serializable;
import java.util.ArrayList;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MaseProblem extends Problem implements GroupedProblemForm, SimpleProblemForm {

    private static final long serialVersionUID = 1L;

    public static final String P_EVAL_NUMBER = "number-evals";
    public static final String P_EVAL = "eval";
    protected EvaluationFunction[] evalFunctions;

    public static final String P_TRIALS_MERGE = "trials-merge";
    public static final String V_MERGE_BEST = "best", V_MERGE_MEAN = "mean", V_MERGE_MEDIAN = "median";
    protected String mergeMode;

    public static final String P_SEED = "seed";
    public static final String V_RANDOM_SEED = "random";
    protected boolean sameSeed;
    protected long seed;

    public static final String P_CONTROLLER_FACTORY = "controller-factory";
    protected ControllerFactory controllerFactory;

    protected EvaluationCounter counter;

    protected static class EvaluationCounter implements Serializable {

        private static final long serialVersionUID = 1L;

        private int counter = 0;

        protected synchronized void increment(EvolutionState state) {
            counter++;
            // The termination by numEvaluations in SimpleEvolutionState is quite stupid
            // It only accounts for the population size, and assumes the same number of
            // populations and individuals in the whole run
            if(state.numEvaluations > 0) {
                if(counter >= state.numEvaluations) { // force termination now
                    state.numGenerations = state.generation + 1;
                } else if(state.generation == state.numGenerations - 1) { // should not terminate yet
                    state.numGenerations++;
                }
            }
        }

        protected int value() {
            return counter;
        }
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        base = defaultBase();

        /* evaluation */
        if (!state.parameters.exists(base.push(P_EVAL_NUMBER), null)) {
            state.output.warning("Parameter not exists. Going to use just 1 evaluation.", base.push(P_EVAL_NUMBER));
        }
        int nEvals = state.parameters.getIntWithDefault(base.push(P_EVAL_NUMBER), null, 1);
        evalFunctions = new EvaluationFunction[nEvals];
        for (int i = 0; i < nEvals; i++) {
            evalFunctions[i] = (EvaluationFunction) state.parameters.getInstanceForParameter(base.push(P_EVAL).push("" + i), base.push(P_EVAL), EvaluationFunction.class);
            evalFunctions[i].setup(state, base.push(P_EVAL).push("" + i));
        }

        /* trial merge */
        if (!state.parameters.exists(base.push(P_TRIALS_MERGE), null)) {
            mergeMode = V_MERGE_BEST;
            state.output.warning("Parameter not found. Going with best.", base.push(P_TRIALS_MERGE));
        } else {
            mergeMode = state.parameters.getString(base.push(P_TRIALS_MERGE), null);
        }

        String seedString = state.parameters.getStringWithDefault(base.push(P_SEED), null, V_RANDOM_SEED);
        if (seedString.equalsIgnoreCase(V_RANDOM_SEED)) {
            sameSeed = false;
        } else {
            sameSeed = true;
            seed = Long.parseLong(seedString);
        }

        controllerFactory = (ControllerFactory) state.parameters.getInstanceForParameter(base.push(P_CONTROLLER_FACTORY), null, ControllerFactory.class);
        controllerFactory.setup(state, base.push(P_CONTROLLER_FACTORY));
        
        counter = new EvaluationCounter();
    }

    public int getTotalEvaluations() {
        return counter.value();
    }
    
    @Override
    public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment, boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            for (Individual ind : pop.subpops[i].individuals) {
                if (prepareForFitnessAssessment[i]) {
                    ind.fitness.trials = new ArrayList();
                }
            }
        }
    }

    @Override
    public void postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            for (Individual ind : pop.subpops[i].individuals) {
                if (assessFitness[i]) {
                    ExpandedFitness[] trials = new ExpandedFitness[ind.fitness.trials.size()];
                    for (int k = 0; k < trials.length; k++) {
                        trials[k] = (ExpandedFitness) ind.fitness.trials.get(k);
                    }
                    switch (mergeMode) {
                        case V_MERGE_MEAN:
                            ind.fitness.setToMeanOf(state, trials);
                            break;
                        case V_MERGE_MEDIAN:
                            ind.fitness.setToMedianOf(state, trials);
                            break;
                        case V_MERGE_BEST:
                            ind.fitness.setToBestOf(state, trials);
                            break;
                    }
                    ind.fitness.trials = null;
                    ind.evaluated = true;
                }
            }
        }
    }

    @Override
    public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        GroupController gc = controllerFactory.createController(state, ind);
        EvaluationResult[] eval = evaluateSolution(gc, nextSeed(state, threadnum));
        counter.increment(state);
        for (int i = 0; i < ind.length; i++) {
            if (updateFitness[i]) {
                ExpandedFitness trial = (ExpandedFitness) ind[i].fitness.clone();
                trial.setEvaluationResults(state, eval, subpops[i]);
                trial.setContext(ind);
                trial.trials = null;
                ind[i].fitness.trials.add(trial);
            }
        }
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        GroupController gc = controllerFactory.createController(state, ind);
        EvaluationResult[] eval = evaluateSolution(gc, nextSeed(state, threadnum));
        counter.increment(state);
        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        fit.setEvaluationResults(state, eval, subpopulation);
        ind.evaluated = true;
    }
   
    public EvaluationFunction[] getEvalFunctions() {
        return evalFunctions;
    }
    
    public void setEvalFunctions(EvaluationFunction[] evals) {
        this.evalFunctions = evals;
    }

    public ControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    public long nextSeed(EvolutionState state, int threadnum) {
        if (sameSeed) {
            return seed;
        } else {
            synchronized(state.random[threadnum]) {
                return state.random[threadnum].nextLong();
            }
        }
    }
    
    public abstract EvaluationResult[] evaluateSolution(GroupController gc, long seed);

}
