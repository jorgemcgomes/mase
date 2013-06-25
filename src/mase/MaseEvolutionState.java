/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import static ec.EvolutionState.R_FAILURE;
import static ec.EvolutionState.R_NOTDONE;
import static ec.EvolutionState.R_SUCCESS;
import ec.coevolve.MultiPopCoevolutionaryEvaluator2;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class MaseEvolutionState extends SimpleEvolutionState {

    public static final String P_NUM_POST_EVAL = "num-post-eval";
    public static final String P_POST_EVAL = "post-eval";
    public PostEvaluator[] postEvaluators;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        int numPosts = state.parameters.getIntWithDefault(new Parameter(P_NUM_POST_EVAL), null, 0);
        postEvaluators = new PostEvaluator[numPosts];
        for (int i = 0; i < numPosts; i++) {
            Parameter b = new Parameter(P_POST_EVAL).push("" + i);
            postEvaluators[i] = (PostEvaluator) state.parameters.getInstanceForParameter(
                    b, null, PostEvaluator.class);
            postEvaluators[i].setup(state, b);
        }
    }

    @Override
    public int evolve() {
        if (generation > 0) {
            output.message("Generation " + generation);
        }

        // EVALUATION
        statistics.preEvaluationStatistics(this);
        evaluator.evaluatePopulation(this);
        for (PostEvaluator postEval : postEvaluators) {
            postEval.processPopulation(this);
        }
        if(evaluator instanceof MultiPopCoevolutionaryEvaluator2) { // dirty hack
            ((MultiPopCoevolutionaryEvaluator2) evaluator).afterCoevolutionaryEvaluation(this, this.population, null);
        }
        statistics.postEvaluationStatistics(this);

        // SHOULD WE QUIT?
        if (evaluator.runComplete(this) && quitOnRunComplete) {
            output.message("Found Ideal Individual");
            return R_SUCCESS;
        }

        // SHOULD WE QUIT?
        if (generation == numGenerations - 1) {
            return R_FAILURE;
        }

        // PRE-BREEDING EXCHANGING
        statistics.prePreBreedingExchangeStatistics(this);
        population = exchanger.preBreedingExchangePopulation(this);
        statistics.postPreBreedingExchangeStatistics(this);

        String exchangerWantsToShutdown = exchanger.runComplete(this);
        if (exchangerWantsToShutdown != null) {
            output.message(exchangerWantsToShutdown);
            return R_SUCCESS;
        }

        // BREEDING
        statistics.preBreedingStatistics(this);

        population = breeder.breedPopulation(this);

        // POST-BREEDING EXCHANGING
        statistics.postBreedingStatistics(this);

        // POST-BREEDING EXCHANGING
        statistics.prePostBreedingExchangeStatistics(this);
        population = exchanger.postBreedingExchangePopulation(this);
        statistics.postPostBreedingExchangeStatistics(this);

        // INCREMENT GENERATION AND CHECKPOINT
        generation++;
        if (checkpoint && generation % checkpointModulo == 0) {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
        }

        return R_NOTDONE;
    }
}
