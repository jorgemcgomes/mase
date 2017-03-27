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
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author jorge
 */
public class ExpandedFitness extends SimpleFitness {

    public static final String AUTO_FITNESS_PREFIX = "eval.";
    public static final String P_FITNESS_EVAL_INDEX = "fitness-index";
    public static final String FITNESS_SCORE = "fitness";
    private static final long serialVersionUID = 1L;
    protected int fitnessIndex;

    protected EvaluationResult[] evalResults;
    protected LinkedHashMap<String,Double> scores;
    protected int subpop;
    
    
    @Override
    public Parameter defaultBase() {
        return new Parameter(P_FITNESS);
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.fitnessIndex = state.parameters.getIntWithDefault(base.push(P_FITNESS_EVAL_INDEX), defaultBase().push(P_FITNESS_EVAL_INDEX), 0);
    }

    public EvaluationResult[] getEvaluationResults() {
        return evalResults;
    }
    
    public void setEvaluationResults(EvolutionState state, EvaluationResult[] br, int subpop) {
        this.evalResults = br;
        this.subpop = subpop;
        double fit = getFitnessScoreAux();
        this.setFitness(state, fit, false);
        scores = new LinkedHashMap<>();
        scores.put(FITNESS_SCORE, fit);
        
        // Automatically add all fitness results as scores
        for(int i = 0 ; i < br.length ; i++) {
            EvaluationResult e = br[i];
            if(e instanceof FitnessResult) {
                FitnessResult fr = (FitnessResult) e;
                scores.put(AUTO_FITNESS_PREFIX + i, fr.value());
            }
        }
    }
    
    public EvaluationResult getCorrespondingEvaluation(int index) {
        EvaluationResult er = evalResults[index];
        if (er instanceof SubpopEvaluationResult) {
            SubpopEvaluationResult aer = (SubpopEvaluationResult) er;
            er = (EvaluationResult) aer.getSubpopEvaluation(getCorrespondingSubpop());
        }
        return er;
    }    
    
    public LinkedHashMap<String,Double> scores() {
        return scores;
    }
    
    public double getScore(String score) {
        return scores.get(score);
    }
    
    public void setFitnessIndex(int index) {
        this.fitnessIndex = index;
    }
    
    public int getFitnessIndex() {
        return fitnessIndex;
    }

    public int getCorrespondingSubpop() {
        return subpop;
    }
    
    public double getFitnessScore() {
        return scores.get(FITNESS_SCORE);
    }

    private double getFitnessScoreAux() {
        EvaluationResult er = getCorrespondingEvaluation(fitnessIndex);
        return (double) er.value();
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
            EvaluationResult[] evalRes = new EvaluationResult[((ExpandedFitness) fitnesses[0]).evalResults.length];
            for (int i = 0; i < evalRes.length; i++) { // for each evaluation function
                EvaluationResult[] evalTrials = new EvaluationResult[fitnesses.length];
                for (int j = 0; j < fitnesses.length; j++) { // for each trial
                    evalTrials[j] = ((ExpandedFitness) fitnesses[j]).evalResults[i];
                }
                evalRes[i] = (EvaluationResult) evalTrials[0].mergeEvaluations(evalTrials);
            }
            this.setEvaluationResults(state, evalRes, subpop);
            
            // Context corresponding to the individual with the median fitness score
            Fitness[] sortedFits = Arrays.copyOf(fitnesses, fitnesses.length);
            Arrays.sort(sortedFits, new Comparator<Fitness>() {
                @Override
                public int compare(Fitness o1, Fitness o2) {
                    return Double.compare(((ExpandedFitness) o2).getFitnessScore(), 
                            ((ExpandedFitness) o1).getFitnessScore());
                }
            });
            this.setContext(sortedFits[sortedFits.length / 2].getContext());
        }
    }

    @Override
    public String toString() {
        String str = super.fitness() + "";
        if(scores == null) {
            return str + " ; no_set_results";
        }
        for(Entry<String,Double> e : scores.entrySet()) {
            str += " ; " + e.getKey() + ":" + e.getValue();
        }
        return str;
    }
}
