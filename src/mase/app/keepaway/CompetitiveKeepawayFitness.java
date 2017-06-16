/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepawayFitness extends MasonEvaluation {

    public static final String P_PASS_LENGTH = "pass-length";
    private double passLength;
    private CompoundEvaluationResult fitnessResult;
    private int numPasses;
    private transient Keeper lastKeeper;
    private Double2D lastBallPos;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.passLength = state.parameters.getDouble(base.push(P_PASS_LENGTH), null);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        this.numPasses = 0;
        this.lastKeeper = null;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        Keepaway kw = (Keepaway) sim;
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if ((lastKeeper == null || k != lastKeeper)
                        && (lastBallPos == null || lastBallPos.distance(kw.ball.getLocation()) > passLength)) {
                    numPasses++;
                }
                lastKeeper = k;
                lastBallPos = kw.ball.getLocation();
                return;
            }
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        fitnessResult = new CompoundEvaluationResult(new EvaluationResult[] {
            new FitnessResult(100f +  numPasses, FitnessResult.MergeMode.arithmetic),
            new FitnessResult(100f - numPasses, FitnessResult.MergeMode.arithmetic)
        });
    }

    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }
    
}
