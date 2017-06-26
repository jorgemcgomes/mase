/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mase.evaluation.BehaviourResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
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
        behavIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), null);
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        EvaluationResult er = sim.currentEvals[behavIndex].getResult();
        if(er instanceof CompoundEvaluationResult) { // only supports single agent
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
        public static final double MAX = 1000;

        public BehaviourVarianceFitnessResult(BehaviourResult reference) {
            super(0);
            this.reference = reference;
        }

        @Override
        public FitnessResult mergeEvaluations(Collection results) {
            List<BehaviourResult> brs = new ArrayList<>(results.size());
            for(Object o : results) {
                brs.add(((BehaviourVarianceFitnessResult) o).reference);
            }
            BehaviourResult merged = (BehaviourResult) brs.get(0).mergeEvaluations(brs);
            double sum = 0;
            for (BehaviourResult b : brs) {
                sum += merged.distanceTo(b);
            }
            // ensure that the fitness value is positive
            // minimise sum of squared errors
            return new FitnessResult(MAX - sum / brs.size());
        }

    }
}
