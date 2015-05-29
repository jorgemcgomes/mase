/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author jorge
 */
public class ExpandedFitness extends SimpleFitness {

    public static final String P_FITNESS_EVAL_INDEX = "fitness-index";
    public static final String FITNESS_SCORE = "fitness";
    protected int fitnessIndex;

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_FITNESS);
    }

    protected EvaluationResult[] evalResults;
    protected LinkedHashMap<String,Float> scores;
    protected int subpop;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.fitnessIndex = state.parameters.getIntWithDefault(base.push(P_FITNESS_EVAL_INDEX), defaultBase().push(P_FITNESS_EVAL_INDEX), 0);
    }

    public void setEvaluationResults(EvolutionState state, EvaluationResult[] br, int subpop) {
        this.evalResults = br;
        this.subpop = subpop;
        float fit = getFitnessScoreAux();
        this.setFitness(state, fit, false);
        scores = new LinkedHashMap<String,Float>();
        scores.put(FITNESS_SCORE, getFitnessScoreAux());
    }
    
    public LinkedHashMap<String,Float> scores() {
        return scores;
    }
    
    public void setFitnessIndex(int index) {
        this.fitnessIndex = index;
    }

    public int getCorrespondingSubpop() {
        return subpop;
    }

    public EvaluationResult[] getEvaluationResults() {
        return evalResults;
    }
    
    public float getFitnessScore() {
        return scores.get(FITNESS_SCORE);
    }

    private float getFitnessScoreAux() {
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
        }
    }
}
