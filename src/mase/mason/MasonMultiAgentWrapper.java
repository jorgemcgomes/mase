/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;

/**
 *
 * @author jorge
 */
public class MasonMultiAgentWrapper extends MasonEvaluation {

    public static final String P_AGENT_EVALUATION_FUNCTION = "fun";
    private static final long serialVersionUID = 1L;
    private SubpopEvaluationResult ser;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        
    }    
    
    @Override
    protected void postSimulation() {
        super.postSimulation(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void evaluate() {
        super.evaluate(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void preSimulation() {
        super.preSimulation(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public EvaluationResult getResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
