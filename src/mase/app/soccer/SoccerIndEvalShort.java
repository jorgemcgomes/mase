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
import mase.mason.MasonSimState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathArrays;

/**
 *
 * @author jorge
 */
public class SoccerIndEvalShort extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private SubpopEvaluationResult ser;
    //private double[][] initDists;
    private double[][] accumDists;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        Soccer soc = (Soccer) sim;
//        initDists = new double[soc.leftTeam.size()][];
        accumDists = new double[soc.leftTeam.size()][];
/*        for (int a = 0; a < soc.leftTeam.size(); a++) {
            initDists[a] = computeDistances(soc.leftTeam.get(a));
        }*/
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Soccer soc = (Soccer) sim;
        for (int a = 0; a < soc.leftTeam.size(); a++) {
            double[] d = computeDistances(soc.leftTeam.get(a), (Soccer) sim);
            accumDists[a] = accumDists[a] == null ? d : MathArrays.ebeAdd(accumDists[a], d);
        }
    }

    private double[] computeDistances(SoccerAgent sa, Soccer soc) {
        double[] res = new double[2];
        // Distance to opponent goal
        res[0] = sa.distanceTo(sa.oppGoal);

        // Distance to ball
        res[1] = sa.distanceTo(soc.ball);
        return res;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        Soccer soc = (Soccer) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[soc.leftTeam.size()];
        for (int a = 0; a < res.length; a++) {
            SoccerAgent sa = soc.leftTeam.get(a);
            for(int i = 0 ; i < accumDists[a].length ; i++) {
                accumDists[a][i] = (accumDists[a][i] / currentEvaluationStep /*- initDists[a][i]*/) / soc.field.width;
            }
            double[] behav = ArrayUtils.add(accumDists[a], soc.referee.scorer == sa ? 1 : 0);
            
            res[a] = new VectorBehaviourResult(behav);
        }
        this.ser = new SubpopEvaluationResult(res);
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
