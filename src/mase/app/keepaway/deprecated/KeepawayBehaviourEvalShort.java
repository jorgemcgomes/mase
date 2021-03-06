/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class KeepawayBehaviourEvalShort extends MasonEvaluation {

    private VectorBehaviourResult res;
    public static final String P_PASS_LENGTH = "pass-length";
    public static final String P_PASS_NORMALIZATION = "pass-normalization";
    private double passLength;
    private int numPasses;
    private transient Keeper lastKeeper;
    private Double2D lastBallPos;
    private double keeperDispersion;
    private int maxSteps;
    private int passesNormalization;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.passLength = state.parameters.getDouble(base.push(P_PASS_LENGTH), null);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
        this.passesNormalization = state.parameters.getInt(base.push(P_PASS_NORMALIZATION), null);
    }

    @Override
    protected void preSimulation() {
        this.numPasses = 0;
        this.lastKeeper = null;
        this.keeperDispersion = 0;
    }

    @Override
    protected void evaluate() {
        Keepaway kw = (Keepaway) sim;
        // number of passes
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

        // keeper dispersion
        MutableDouble2D centre = new MutableDouble2D(0, 0);
        for (Keeper k : kw.keepers) {
            centre.addIn(k.getLocation());
        }
        centre.multiplyIn(1.0 / kw.keepers.size());
        for (Keeper k : kw.keepers) {
            keeperDispersion += k.getLocation().distance(centre.x, centre.y);
        }
    }

    @Override
    protected void postSimulation() {
        Keepaway kw = (Keepaway) sim;
        double steps = sim.schedule.getSteps();
        this.res = new VectorBehaviourResult(
                numPasses / (double) passesNormalization,
                steps / maxSteps,
                 (keeperDispersion / kw.keepers.size() / currentEvaluationStep / (kw.par.ringSize / 2)));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
