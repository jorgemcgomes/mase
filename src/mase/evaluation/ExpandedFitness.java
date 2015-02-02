/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.Arrays;
import java.util.Comparator;

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
    protected int subpop;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.fitnessIndex = state.parameters.getIntWithDefault(base.push(P_FITNESS_EVAL_INDEX), defaultBase().push(P_FITNESS_EVAL_INDEX), 0);
    }

    public void setEvaluationResults(EvolutionState state, EvaluationResult[] br, int subpop) {
        this.evalResults = br;
        this.subpop = subpop;
        this.setFitness(state, getFitnessScore(), false);
    }

    public int getCorrespondingSubpop() {
        return subpop;
    }

    public EvaluationResult[] getEvaluationResults() {
        return evalResults;
    }

    public float getFitnessScore() {
        EvaluationResult er = evalResults[fitnessIndex];
        if (er instanceof SubpopEvaluationResult) {
            SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
            return (Float) ser.getSubpopEvaluation(subpop).value();
        } else {
            return (Float) er.value();
        }
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
        this.subpop = ((ExpandedFitness) fitnesses[0]).subpop;
        if (fitnesses.length == 1) {
            // Just one, pass the information
            this.setEvaluationResults(state, ((ExpandedFitness) fitnesses[0]).evalResults, subpop);
            this.setContext(fitnesses[0].getContext());
        } else {
            // Merge evaluation results
            EvaluationResult[] evalFunctions = new EvaluationResult[((ExpandedFitness) fitnesses[0]).evalResults.length];
            for (int i = 0; i < evalFunctions.length; i++) { // for each evaluation function
                EvaluationResult[] evalTrials = new EvaluationResult[fitnesses.length];
                for (int j = 0; j < fitnesses.length; j++) { // for each trial
                    evalTrials[j] = ((ExpandedFitness) fitnesses[j]).evalResults[i];
                }
                evalFunctions[i] = (EvaluationResult) evalTrials[0].mergeEvaluations(evalTrials);
            }
            this.setEvaluationResults(state, evalFunctions, subpop);
            
            // Context corresponding to the individual with the median fitness score
            Fitness[] sortedFits = Arrays.copyOf(fitnesses, fitnesses.length);
            Arrays.sort(sortedFits, new Comparator<Fitness>() {
                @Override
                public int compare(Fitness o1, Fitness o2) {
                    return Float.compare(((ExpandedFitness) o2).getFitnessScore(), 
                            ((ExpandedFitness) o1).getFitnessScore());
                }
            });
            this.setContext(sortedFits[sortedFits.length / 2].getContext());
            
            /*Individual[] c = null;
            float worstScore = Float.MAX_VALUE;
            for(Fitness f : fitnesses) {
                if(((ExpandedFitness) f).getFitnessScore() < worstScore) {
                    worstScore = ((ExpandedFitness) f).getFitnessScore();
                    c = f.getContext();
                }
            }
            this.setContext(c);*/
        }

    }
}
