/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

import ec.EvolutionState;
import ec.util.Parameter;
import evolutionaryrobotics.JBotEvolver;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.EvaluationFunction;
import mase.mason.MasonEvaluation;
import simulation.Updatable;

/**
 *
 * @author jorge
 */
public abstract class JBotEvaluation implements EvaluationFunction, Updatable {

    public static final String P_DEFAULT = "jbot-eval";
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        ; // nothing to do here
    }
    
    public void setup(JBotEvolver evo) {
        ;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_DEFAULT);
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MasonEvaluation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
}
