/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

/**
 *
 * @author jorge
 */
public class FixedValuesController implements EncodableAgentController {

    private static final long serialVersionUID = 1L;

    private double[] values;

    public FixedValuesController(double[] values) {
        this.values = values;
    }
    
    public FixedValuesController() {
        this.values = null;
    }
    
    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    @Override
    public double[] processInputs(double[] input) {
        return values;
    }

    @Override
    public void reset() {
        // stateless
    }

    @Override
    public AgentController clone() {
        return new FixedValuesController(values);
    }

    @Override
    public double[] encode() {
        return values;
    }

    @Override
    public void decode(double[] params) {
        this.setValues(params);
    }

}
