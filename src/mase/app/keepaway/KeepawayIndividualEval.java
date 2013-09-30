/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.evaluation.AgentEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class KeepawayIndividualEval extends MasonEvaluation {

    private int passesNormalization;
    private double minPass;
    private float[] passNumber;
    private float[] passLength;
    private float[] distanceToOthers;
    private float[] movement;
    private int[] allCount;
    private int lastKeeper;
    private Double2D lastPossession;
    private AgentEvaluationResult evaluation;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.minPass = state.parameters.getDouble(base.push(KeepawayBehaviourEval.P_PASS_LENGTH), null);
        this.passesNormalization = state.parameters.getInt(base.push(KeepawayBehaviourEval.P_PASS_NORMALIZATION), null);
    }

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Keepaway kw = (Keepaway) sim;
        this.lastKeeper = -1;
        this.passNumber = new float[kw.keepers.size()];
        this.passLength = new float[kw.keepers.size()];
        this.distanceToOthers = new float[kw.keepers.size()];
        this.movement = new float[kw.keepers.size()];
        this.allCount = new int[kw.keepers.size()];
    }

    @Override
    protected void evaluate() {
        // should be similar to group evaluation
        // -- number of good passes (equivalent to group)
        // -- average pass length (also equivalent)
        // -- average distance to other keepers (equivalent)
        // -- average movement (not equivalent, but makes sense)

        Keepaway kw = (Keepaway) sim;
        // number of good passes and pass length
        for (int i = 0; i < kw.keepers.size(); i++) {
            Keeper k = kw.keepers.get(i);
            if (k.hasPossession) {
                if (lastKeeper != -1 && i != lastKeeper) { // pass between different agents
                    double passDist = lastPossession.distance(kw.ball.getLocation());
                    if (passDist > minPass) {
                        passNumber[lastKeeper]++;
                    }
                    allCount[lastKeeper]++;
                    passLength[lastKeeper] += passDist;
                }
                lastKeeper = i;
                lastPossession = kw.ball.getLocation();
                return;
            }
        }

        // average distance to other keepers
        for (int i = 0; i < kw.keepers.size(); i++) {
            Keeper k = kw.keepers.get(i);
            for (int j = 0; j < kw.keepers.size(); j++) {
                if (i != j) {
                    Keeper k2 = kw.keepers.get(j);
                    distanceToOthers[i] += k.distanceTo(k2);
                }
            }
        }

        // average movement
        for (int i = 0; i < kw.keepers.size(); i++) {
            Keeper k = kw.keepers.get(i);
            movement[i] += k.getSpeed() / kw.par.moveSpeed[i];
        }
    }

    @Override
    protected void postSimulation() {
        Keepaway kw = (Keepaway) sim;
        for (int i = 0; i < kw.keepers.size(); i++) {
            passNumber[i] /= passesNormalization;
            if (allCount[i] > 0) {
                passLength[i] = (float) (passLength[i] / allCount[i] / kw.par.size);
            }
            distanceToOthers[i] = (float) (distanceToOthers[i] / currentEvaluationStep / (kw.keepers.size() - 1) / kw.par.size);
            movement[i] /= currentEvaluationStep;
        }
    }

    @Override
    public EvaluationResult getResult() {
        Keepaway kw = (Keepaway) sim;
        if (evaluation == null) {
            VectorBehaviourResult[] res = new VectorBehaviourResult[kw.keepers.size()];
            for (int i = 0; i < res.length; i++) {
                float[] b = new float[]{passNumber[i], passLength[i], distanceToOthers[i], movement[i]};
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new AgentEvaluationResult(res);
        }
        return evaluation;
    }
}