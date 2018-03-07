/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.metrics;

import mase.app.maze.MazeTask;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import net.jafama.FastMath;
import org.apache.commons.math3.util.MathUtils;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CircularFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    protected double initialOri;
    protected Double2D initialPos;
    protected double finalOri;
    protected double oriDelta;
    protected Double2D finalPos;
    protected Double2D displacement;
    protected double targetOrientation;
    protected double oriError;
    private FitnessResult fr;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        SmartAgentProvider smp = (SmartAgentProvider) sim;
        initialOri = smp.getSmartAgents().get(0).orientation2D();
        initialPos = smp.getSmartAgents().get(0).getLocation();
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        SmartAgentProvider smp = (SmartAgentProvider) sim;
        oriDelta = MathUtils.normalizeAngle(smp.getSmartAgents().get(0).orientation2D() - initialOri, 0);
        displacement = smp.getSmartAgents().get(0).getLocation().subtract(initialPos).rotate(initialOri);
        
        targetOrientation = getTargetOrientation(displacement);
        oriError = getOrientationError(oriDelta, targetOrientation);
        double fit = getOrientationErrorFitness(oriError);
        this.fr = new FitnessResult(fit);
    }

    public static double calculateOrientationFitness(Double2D pos, double orientation) {
        double t = getTargetOrientation(pos);
        double e = getOrientationError(orientation, t);
        return getOrientationErrorFitness(e);
    }
    
    private static double getOrientationErrorFitness(double error) {
        return 1- Math.abs(error) / Math.PI;
    }
    
    private static double getOrientationError(double orientation, double targetOrientation) {
        return MathUtils.normalizeAngle(targetOrientation - orientation, 0);
    }

    // [-PI, PI]
    private static double getTargetOrientation(Double2D pos) {
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
