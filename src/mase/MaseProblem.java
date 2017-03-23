/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.controllers.HomogeneousGroupController;
import mase.controllers.MultiAgentControllerIndividual;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.spec.AbstractHybridExchanger;

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
            try { // Try to fix issues with random number generation (concurrency aparently)
                Thread.sleep(3000); // EXPERIMENTAL
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }            
        } else {
            sameSeed = true;
            seed = Long.parseLong(seedString);
        }
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

    // TODO: Bad dependencies -- should be improved with interfaces
    public GroupController createController(EvolutionState state, Individual... ind) {
        ArrayList<AgentController> acs = new ArrayList<>();
        for (Individual ind1 : ind) {
            if (ind1 instanceof AgentControllerIndividual) {
                acs.add(((AgentControllerIndividual) ind1).decodeController());
            } else if (ind1 instanceof MultiAgentControllerIndividual) {
                AgentController[] as = ((MultiAgentControllerIndividual) ind1).decodeControllers();
                acs.addAll(Arrays.asList(as));
            }
        }
        GroupController gc;
        if (acs.size() == 1) {
            gc = new HomogeneousGroupController(acs.get(0));
        } else {
            AgentController[] acsArray = new AgentController[acs.size()];
            acs.toArray(acsArray);
            
            if (state.exchanger instanceof AbstractHybridExchanger) {
                AbstractHybridExchanger exc = (AbstractHybridExchanger) state.exchanger;
                int[] allocations = exc.getAllocations();
                AgentController[] temp = new AgentController[allocations.length];
                for (int i = 0; i < allocations.length; i++) {
                    temp[i] = acsArray[allocations[i]].clone();
                }
                acsArray = temp;
            }
            gc = new HeterogeneousGroupController(acsArray);
        }
        return gc;
    }

    @Override
    public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        GroupController gc = createController(state, ind);
        EvaluationResult[] eval = evaluateSolution(gc, nextSeed(state, threadnum));
        /* Save results */
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
        GroupController gc = createController(state, ind);
        EvaluationResult[] eval = evaluateSolution(gc, nextSeed(state, threadnum));
        ExpandedFitness fit = (ExpandedFitness) ind.fitness;
        fit.setEvaluationResults(state, eval, subpopulation);
        ind.evaluated = true;
    }

    public EvaluationFunction[] getEvalFunctions() {
        return evalFunctions;
    }

    public abstract EvaluationResult[] evaluateSolution(GroupController gc, long seed);

    public synchronized long nextSeed(EvolutionState state, int threadnum) {
        if (sameSeed) {
            return seed;
        } else {
            return state.random[threadnum].nextLong();
        }
    }

}
