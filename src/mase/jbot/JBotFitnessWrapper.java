/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import simulation.Simulator;

/**
 *
 * @author jorge
 */
public class JBotFitnessWrapper extends JBotEvaluation {

    protected EvaluationFunction evalFunction = null;
    protected FitnessResult result;
    protected JBotEvolver evo;

    @Override
    public void setup(JBotEvolver evo) {
        super.setup(evo);
        this.evo = evo;
    }

    @Override
    public void update(Simulator simulator) {
        if (evalFunction == null) {
            evalFunction = evo.getEvaluationFunction();
        }
        simulator.getCallbacks();
        evalFunction.update(simulator);
    }

    @Override
    public EvaluationResult getResult() {
        if (result == null) {
            result = new FitnessResult((float) evalFunction.getFitness(), FitnessResult.ARITHMETIC);
        }
        return result;
    }

}
