/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class ExpandedFitness extends SimpleFitness {

    public static final String P_FITNESS_EVAL_INDEX = "fitness-index";
    protected int fitnessIndex;

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_FITNESS);
    }
    
    protected EvaluationResult[] evalResults;
    protected float fitnessScore;
    protected int subpop;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.fitnessIndex = state.parameters.getIntWithDefault(base.push(P_FITNESS_EVAL_INDEX), defaultBase().push(P_FITNESS_EVAL_INDEX), 0);
    }

    public void setCorrespondingSubpop(int n) {
        this.subpop = n;
    }

    public int getCorrespondingSubpop() {
        return subpop;
    }

    public void setEvaluationResults(EvaluationResult[] br) {
        this.evalResults = br;
    }

    public EvaluationResult[] getEvaluationResults() {
        return evalResults;
    }

    public float getFitnessScore() {
        return (Float) evalResults[fitnessIndex].value();
    }

    /**
     * Requires fitness value to be set in each of the fitnesses
     *
     * @param state
     * @param fitnesses
     */
    @Override
    public void setToMeanOf(EvolutionState state, Fitness[] fitnesses) {
        super.setToMeanOf(state, fitnesses);
        if (fitnesses.length == 1) {
            EvaluationResult[] otherChars = ((ExpandedFitness) fitnesses[0]).evalResults;
            this.evalResults = Arrays.copyOf(otherChars, otherChars.length);
            this.setContext(fitnesses[0].getContext());
        } else {
            this.evalResults = new EvaluationResult[((ExpandedFitness) fitnesses[0]).evalResults.length];
            for (int i = 0; i < evalResults.length; i++) { // for each evaluation function
                EvaluationResult[] evals = new EvaluationResult[fitnesses.length];
                for (int j = 0; j < fitnesses.length; j++) { // for each trial
                    evals[j] = ((ExpandedFitness) fitnesses[j]).evalResults[i];
                }
                this.evalResults[i] = (EvaluationResult) evals[0].mergeEvaluations(evals);
            }
            // How to fill the context in this case? Context = best fitness
            float maxFit = Float.NEGATIVE_INFINITY;
            int best = -1;
            for (int j = 0; j < fitnesses.length; j++) {
                float f = ((ExpandedFitness) fitnesses[j]).fitness;
                if (f > maxFit) {
                    maxFit = f;
                    best = j;
                }
            }
            this.setContext(fitnesses[best].getContext());
        }
    }
}
