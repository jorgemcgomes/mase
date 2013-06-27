/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class KeepawayPassesFitness extends MasonEvaluation {

    public static final String P_PASS_LENGTH = "pass-length";
    private double passLength;
    private FitnessResult fitnessResult;
    private int numPasses;
    private transient Keeper lastKeeper;
    private Double2D lastBallPos;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.passLength = state.parameters.getDouble(base.push(P_PASS_LENGTH), null);
    }

    @Override
    protected void preSimulation() {
        this.numPasses = 0;
        this.lastKeeper = null;
    }

    @Override
    protected void evaluate() {
        Keepaway kw = (Keepaway) sim;
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if ((lastKeeper == null || k != lastKeeper) && 
                        (lastBallPos == null || lastBallPos.distance(kw.ball.getLocation()) > passLength)) {
                    numPasses++;
                    lastBallPos = kw.ball.getLocation();
                    lastKeeper = k;
                }
                return;
            }
        }
    }

    @Override
    protected void postSimulation() {
        fitnessResult = new FitnessResult((float) numPasses);
    }

    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }
}
