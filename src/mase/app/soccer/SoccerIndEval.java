/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class SoccerIndEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private SubpopEvaluationResult ser;
    private MutableDouble2D[] fieldPos;
    private double[] distToBall;
    private double[] distToTeammate;
    private double[] distToOpponent;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Soccer soc = (Soccer) sim;
        fieldPos = new MutableDouble2D[soc.leftTeam.size()];
        for (int i = 0; i < fieldPos.length; i++) {
            fieldPos[i] = new MutableDouble2D(0, 0);
        }
        distToBall = new double[fieldPos.length];
        distToTeammate = new double[fieldPos.length];
        distToOpponent = new double[fieldPos.length];
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        Soccer soc = (Soccer) sim;
        for (int a = 0; a < soc.leftTeam.size(); a++) {
            SoccerAgent sa = soc.leftTeam.get(a);

            // Agent location
            fieldPos[a].addIn(sa.getLocation());

            // Distance to ball
            distToBall[a] += sa.distanceTo(soc.ball.getLocation());

            // Distance to closest teammate
            double min = Double.POSITIVE_INFINITY;
            for (SoccerAgent other : sa.teamMates) {
                min = Math.min(min, sa.distanceTo(other));
            }
            distToTeammate[a] += min;

            // Distance to closest opponent
            min = Double.POSITIVE_INFINITY;
            for (SoccerAgent other : sa.oppTeam) {
                min = Math.min(min, sa.distanceTo(other));
            }
            distToOpponent[a] += min;
        }
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Soccer soc = (Soccer) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[soc.leftTeam.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = new VectorBehaviourResult(
                    fieldPos[i].x / soc.field.width / currentEvaluationStep,
                    fieldPos[i].y / soc.field.height / currentEvaluationStep,
                    distToBall[i] / soc.field.width / currentEvaluationStep,
                    distToTeammate[i] / soc.field.width / currentEvaluationStep,
                    distToOpponent[i] / soc.field.width / currentEvaluationStep
            );
        }
        this.ser = new SubpopEvaluationResult(res);
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
