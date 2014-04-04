/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyIndividualEval3 extends MasonEvaluation {

    protected float diagonal;
    protected int nAgents;
    protected float maxSpeed;
    protected int avgDistanceSteps;
    protected int[] voidSteps;
    protected float[] preyDist;
    protected float[] captured;
    protected float[] linearSpeed;
    protected float[] predatorMeanDist;
    protected float[] turnSpeed;
    protected float[] xPos;
    protected float[] yPos;
    protected float[] closestDist;
    protected SubpopEvaluationResult evaluation;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.nAgents = state.parameters.getInt(base.pop().pop().push(PredParams.P_NPREDATORS), null);
        this.maxSpeed = state.parameters.getFloat(base.pop().pop().push(PredParams.P_PREDATOR_SPEED), null);
        float size = state.parameters.getInt(base.pop().pop().push(PredParams.P_SIZE), null);
        this.diagonal = (float) Math.sqrt(Math.pow(size, 2) * 2);
    }

    @Override
    public void preSimulation() {
        super.preSimulation();
        preyDist = new float[nAgents];
        captured = new float[nAgents];
        linearSpeed = new float[nAgents];
        predatorMeanDist = new float[nAgents];
        turnSpeed = new float[nAgents];
        xPos = new float[nAgents];
        yPos = new float[nAgents];
        closestDist = new float[nAgents];
        
        avgDistanceSteps = 0;
        voidSteps = new int[nAgents];
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            double d = pred.distanceTo(prey);
            voidSteps[i] = (int) Math.round(d / maxSpeed);
        }
    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            double d = pred.distanceTo(prey);
            if(simState.schedule.getSteps() > voidSteps[i]) {
                preyDist[i] += d;
                avgDistanceSteps++;
            }
            linearSpeed[i] += pred.getSpeed();
            turnSpeed[i] += pred.getTurningSpeed();
            Double2D predLoc = simState.field.getObjectLocation(pred);
            xPos[i] += predLoc.x;
            yPos[i] += predLoc.y;
            float closest = Float.POSITIVE_INFINITY;
            for(Predator pOther : simState.predators) {
                if(pred != pOther) {
                    float dPred = (float) pred.distanceTo(pOther);
                    closest = Math.min(closest, dPred);
                    predatorMeanDist[i] += dPred;
                }
            }
            closestDist[i] += closest;
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            captured[i] += pred.getCaptureCount();
            preyDist[i] /= (diagonal / 2) * avgDistanceSteps;
            linearSpeed[i] /= currentEvaluationStep * maxSpeed;
            predatorMeanDist[i] /= (nAgents -1) * currentEvaluationStep * (diagonal / 2);
            closestDist[i] /=  currentEvaluationStep * (diagonal / 2);
            turnSpeed[i] /= currentEvaluationStep * simState.par.predatorRotateSpeed;
            xPos[i] /= currentEvaluationStep * simState.par.size;
            yPos[i] /= currentEvaluationStep * simState.par.size;
        }
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            VectorBehaviourResult[] res = new VectorBehaviourResult[nAgents];
            for (int i = 0; i < res.length; i++) {
                float[] b = new float[]{captured[i], preyDist[i], predatorMeanDist[i], closestDist[i], linearSpeed[i], turnSpeed[i], xPos[i], yPos[i] };
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new SubpopEvaluationResult(res);
        }
        return evaluation;
    }
}
