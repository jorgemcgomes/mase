/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.novelty.NoveltyEvaluation;

/**
 * This must always come after the behaviour evaluation function
 *
 * @author jorge
 */
public class BehaviourVarianceFitness extends MasonEvaluation {

    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    private int behavIndex;
    private static final long serialVersionUID = 1L;

    private BehaviourVarianceFitnessResult fr = null;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        behavIndex = state.parameters.getInt(base.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX),
                NoveltyEvaluation.DEFAULT_BASE.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX));
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        EvaluationResult er = sim.getCurrentEvalFunctions()[behavIndex].getResult();
        if (er instanceof CompoundEvaluationResult) { // does not really support compountevaluationresults
            fr = new BehaviourVarianceFitnessResult((BehaviourResult) ((CompoundEvaluationResult) er).getEvaluation(0));
        } else {
            fr = new BehaviourVarianceFitnessResult((BehaviourResult) er);
        }
    }

    @Override
    public EvaluationResult getResult() {
        return fr;
    }

    private static class BehaviourVarianceFitnessResult extends FitnessResult {

        private static final long serialVersionUID = 1L;

        private final BehaviourResult reference;
        public static final double MAX = 100;

        public BehaviourVarianceFitnessResult(BehaviourResult reference) {
            super(0);
            this.reference = reference;
        }

        @Override
        public FitnessResult mergeEvaluations(Collection results) {
            List<BehaviourResult> brs = new ArrayList<>(results.size());
            for (Object o : results) {
                brs.add(((BehaviourVarianceFitnessResult) o).reference);
            }
            BehaviourResult merged = (BehaviourResult) brs.get(0).mergeEvaluations(brs);
            double sum = 0;
            for (BehaviourResult b : brs) {
                double d = merged.distanceTo(b);
                sum += d * d;
            }
            // ensure that the fitness value is positive
            // minimise sum of squared errors
            return new FitnessResult(MAX - sum / brs.size());
        }

    }
}
