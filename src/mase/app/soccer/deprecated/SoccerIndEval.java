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

/**
 *
 * @author jorge
 */
public class SoccerIndEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private SubpopEvaluationResult ser;
    private double[] distToOppGoal;
    private double[] distToBall;
    private double[] distToTeammate;
    private double[] distToOpponent;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Soccer soc = (Soccer) sim;
        distToOppGoal = new double[soc.leftTeam.size()];
        distToBall = new double[soc.leftTeam.size()];
        distToTeammate = new double[soc.leftTeam.size()];
        distToOpponent = new double[soc.leftTeam.size()];
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        Soccer soc = (Soccer) sim;
        for (int a = 0; a < soc.leftTeam.size(); a++) {
            SoccerAgent sa = soc.leftTeam.get(a);

            // Distance to opponent goal
            distToOppGoal[a] += sa.distanceTo(sa.oppGoal);

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
        for (int a = 0; a < res.length; a++) {
            int scored = soc.referee.scorers.get(soc.leftTeam.get(a));
            res[a] = new VectorBehaviourResult(
                    distToOppGoal[a] / soc.field.width / currentEvaluationStep,
                    distToBall[a] / soc.field.width / currentEvaluationStep,
                    distToTeammate[a] / soc.field.width / currentEvaluationStep,
                    distToOpponent[a] / soc.field.width / currentEvaluationStep,
                    soc.referee.leftTeamScore > 0 ? (double) scored / soc.referee.leftTeamScore : 0
            );
        }
        this.ser = new SubpopEvaluationResult(res);
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
