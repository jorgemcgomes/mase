/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;

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
        MasonEvaluation behaviourEval = sim.currentEvals[behavIndex];
        BehaviourResult br = (BehaviourResult) behaviourEval.getResult();
        fr = new BehaviourVarianceFitnessResult(br);
    }

    @Override
    public EvaluationResult getResult() {
        return fr;
    }

    private static class BehaviourVarianceFitnessResult extends FitnessResult {

        private static final long serialVersionUID = 1L;

        private final BehaviourResult reference;
        public static final double MAX = 10000;

        public BehaviourVarianceFitnessResult(BehaviourResult reference) {
            super(0);
            this.reference = reference;
        }

        @Override
        public FitnessResult mergeEvaluations(EvaluationResult[] results) {
            BehaviourResult[] brs = new BehaviourResult[results.length];
            for (int i = 0; i < results.length; i++) {
                brs[i] = ((BehaviourVarianceFitnessResult) results[i]).reference;
            }
            BehaviourResult br = (BehaviourResult) brs[0];
            BehaviourResult merged = (BehaviourResult) br.mergeEvaluations(brs);
            double sum = 0;
            for (BehaviourResult b : brs) {
                sum += FastMath.pow2(merged.distanceTo(b));
            }
            // ensure that the fitness value is positive
            // minimise sum of squared errors
            return new FitnessResult(MAX - sum);
        }

    }
}
