/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Exchanger;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.novelty.NoveltyFitness;

/**
 * Not really an exchanger! Just a Singleton that is called before breeding
 *
 * @author jorge
 */
public class SelectionPoolBuilder extends Exchanger {

    public static final String P_SELECTION_THRESHOLD = "selection-threshold";
    private Individual[][] selectionPools;
    private Integer[][] individualsOrigin;
    private double selectionThreshold;
    // stats
    double[] dispersion;
    volatile int[] selfPicked;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.selectionThreshold = state.parameters.getDouble(base.push(P_SELECTION_THRESHOLD), null);
    }
    
    private BehaviourResult getAgentBR(Individual ind) {
        NoveltyFitness nf = (NoveltyFitness) ind.fitness;
        for(EvaluationResult er : nf.getEvaluationResults()) {
            if(er instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
                return (BehaviourResult) ser.getSubpopEvaluation(nf.getCorrespondingSubpop());
            }
        }
        return null;
        //return nf.getNoveltyBehaviour();
    }

    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        if (selectionPools == null) {
            selectionPools = new Individual[state.population.subpops.length][];
            individualsOrigin = new Integer[state.population.subpops.length][];
            // stats
            dispersion = new double[state.population.subpops.length];
            selfPicked = new int[state.population.subpops.length];
        }
        
        // Prepare
        BehaviourResult[][] brs = new BehaviourResult[state.population.subpops.length][];
        for (int i = 0; i < state.population.subpops.length; i++) {
            brs[i] = new BehaviourResult[state.population.subpops[i].individuals.length];
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                brs[i][j] = getAgentBR(state.population.subpops[i].individuals[j]);
            }
        }

        for (int i = 0; i < state.population.subpops.length; i++) {
            // Compute threshold
            double sum = 0;
            for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
                sum += distance(brs[i][j], brs[i]);
            }
            sum /= state.population.subpops[i].individuals.length;
            dispersion[i] = sum;
            double threshold = selectionThreshold * sum;

            ArrayList<Individual> pool = new ArrayList<Individual>();
            ArrayList<Integer> origin = new ArrayList<Integer>();
            // Add all individuals of this subpop
            pool.addAll(Arrays.asList(state.population.subpops[i].individuals));
            for(Individual ind : state.population.subpops[i].individuals) {
                origin.add(i);
            } 
            
            // Add similar individuals of other subpops
            for (int k = 0; k < state.population.subpops.length; k++) {
                if (k != i) {
                    for (int j = 0; j < state.population.subpops[k].individuals.length; j++) {
                        double d = distance(brs[k][j], brs[i]);
                        //System.out.println(d);
                        if (d <= threshold) {
                            pool.add(state.population.subpops[k].individuals[j]);
                            origin.add(k);
                        }
                    }
                }
            }

            individualsOrigin[i] = new Integer[origin.size()];
            origin.toArray(individualsOrigin[i]);
            selectionPools[i] = new Individual[pool.size()];
            pool.toArray(selectionPools[i]);
        }

        return state.population;
    }

    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        // Does not apply
        return state.population;
    }

    @Override
    public String runComplete(EvolutionState state) {
        // Does not apply
        return null;
    }

    public Individual[] getPool(int subpopulation) {
        return selectionPools[subpopulation];
    }
    
    public Integer[] getIndividualsOrigin(int subpopulation) {
        return individualsOrigin[subpopulation];
    }

    protected double distance(BehaviourResult br, BehaviourResult[] all) {
        double d = 0;
        for (BehaviourResult b : all) {
            d += br.distanceTo(b);
        }
        return d / all.length;
    }

    synchronized void logPick(int subpop) {
        selfPicked[subpop]++;
    }
    
}
