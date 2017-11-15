/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;
import org.apache.commons.math3.util.MathUtils;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CircularFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private double initialOri;
    private Double2D initialPos;
    private FitnessResult fr;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        MazeTask mt = (MazeTask) sim;
        initialOri = mt.agent.orientation2D();
        initialPos = mt.agent.getLocation();
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        MazeTask mt = (MazeTask) sim;
        double oriDelta = MathUtils.normalizeAngle(initialOri - mt.agent.orientation2D(), 0);
        Double2D displacement = mt.agent.getLocation().subtract(initialPos);
        double fit = calculateOrientationFitness(displacement, oriDelta);
        this.fr = new FitnessResult(fit);
    }

    public static double calculateOrientationFitness(Double2D pos, double orientation) {
        double diff = MathUtils.normalizeAngle(getTargetOrientation(pos) - orientation, 0);
        return 1 - Math.abs(diff) / Math.PI;
    }

    // [-PI, PI]
    public static double getTargetOrientation(Double2D pos) {
        double b = Math.sqrt((pos.x / 2.0) * (pos.x / 2.0) + (pos.y / 2.0) * (pos.y / 2.0));
        double alpha = FastMath.atan2(pos.y, pos.x);
        double a = b / FastMath.cos(alpha);
        double beta = FastMath.atan2(pos.y, pos.x - a);
        double orientation = pos.x < 0 ? beta - Math.PI : beta;

        return MathUtils.normalizeAngle(orientation, 0);
    }

    @Override
    public EvaluationResult getResult() {
        return fr;
    }

}
