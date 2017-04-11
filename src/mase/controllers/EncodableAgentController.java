/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

/**
 * The classes that implement this interface should also provide a default empty constructor
 * @author jorge
 */
public interface EncodableAgentController extends AgentController {
    
    public double[] encode();
    
    public void decode(double[] params);
    
}
