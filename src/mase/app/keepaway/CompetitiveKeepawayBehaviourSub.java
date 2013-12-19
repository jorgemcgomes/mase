/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepawayBehaviourSub extends MasonEvaluation {

    public static final String P_PASS_LENGTH = "pass-length";
    public static final String P_PASS_NORMALIZATION = "pass-normalization";

    private SubpopEvaluationResult res;

    private double minPass;
    private int passesNormalization;

    private int numPasses;
    private transient Keeper lastKeeper;
    private Double2D lastPossession;
    private float takerDist;

    private int allPassesCount;
    private float passLength;

    float ballDist[];

    float keeperDispersion;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.minPass = state.parameters.getDouble(base.push(P_PASS_LENGTH), null);
        this.passesNormalization = state.parameters.getInt(base.push(P_PASS_NORMALIZATION), null);
    }

    @Override
    protected void preSimulation() {
        this.numPasses = 0;
        this.lastKeeper = null;
        this.passLength = 0;
        this.allPassesCount = 0;
        this.takerDist = 0;
        this.ballDist = new float[2];
        this.keeperDispersion = 0;
    }

    @Override
    protected void evaluate() {
        Keepaway kw = (Keepaway) sim;
        // number of effective passes and pass length
        float d = 0;
        MutableDouble2D centre = new MutableDouble2D(0, 0);
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if (lastKeeper != null && k != lastKeeper) { // pass between different agents
                    double passDist = lastPossession.distance(kw.ball.getLocation());
                    if (passDist > minPass) {
                        numPasses++;
                    }
                    allPassesCount++;
                    passLength += passDist;
                }
                lastKeeper = k;
                lastPossession = kw.ball.getLocation();
                return;
            }

            // distance of keepers to taker
            d += k.distanceTo(kw.takers.get(0));

            // centre of mass
            centre.addIn(k.getLocation());

            // distance to ball
            ballDist[0] += k.distanceTo(kw.ball) / kw.keepers.size();
        }
        takerDist += d / kw.keepers.size();
        centre.multiplyIn(1.0 / kw.keepers.size());

        for (Keeper k : kw.keepers) {
            keeperDispersion += centre.distance(k.getLocation()) / kw.keepers.size();
        }

        ballDist[1] += kw.takers.get(0).distanceTo(kw.ball);
    }

    @Override
    protected void postSimulation() {
        Keepaway kw = (Keepaway) sim;
        float steps = sim.schedule.getSteps();
        this.res = new SubpopEvaluationResult(
                new VectorBehaviourResult(
                        numPasses / (float) passesNormalization,
                        (float) (ballDist[0] / kw.par.size / steps),
                        (float) (takerDist / kw.par.size / steps),
                        (float) (keeperDispersion / kw.par.size / steps),
                        allPassesCount == 0 ? 0 : (float) (passLength / allPassesCount / kw.par.size)),
                new VectorBehaviourResult(
                        numPasses / (float) passesNormalization,
                        (float) (ballDist[0] / kw.par.size / steps),
                        (float) (takerDist / kw.par.size / steps)
                ));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
