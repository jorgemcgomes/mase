/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public class ArbitratorController implements AgentController {

    private static final long serialVersionUID = 1L;

    private AgentController arbitrator;
    private Repertoire repo;
    private MappingFunction mapFun;
    private transient AgentController lastPrimitive = null;

    public ArbitratorController() {
        this(null, null, null);
    }

    public ArbitratorController(AgentController arbitrator, Repertoire repo, MappingFunction fun) {
        this.arbitrator = arbitrator;
        this.repo = repo;
        this.mapFun = fun;
    }

    /**
     * Assumes that the values of the keys are in the range [0,1]
     *
     * @param repo
     */
    public void setRepertoire(Repertoire repo) {
        this.repo = repo;
    }

    public void setArbitrator(AgentController arbitrator) {
        this.arbitrator = arbitrator;
    }
    
    public void setMappingFunction(MappingFunction mapFun) {
        this.mapFun = mapFun;
    }

    @Override
    public double[] processInputs(double[] input) {
        double[] arbitratorOutput = arbitrator.processInputs(input);

        double[] coords = mapFun.outputToCoordinates(arbitratorOutput);
        
        AgentController primitive = repo.nearest(coords);
        if (lastPrimitive == null || primitive != lastPrimitive) {
            primitive.reset();
            lastPrimitive = primitive;
        }
        double[] primitiveOutput = primitive.processInputs(input);
        
        //System.out.println(Arrays.toString(arbitratorOutput));
        //System.out.println(Arrays.toString(coords));
        //System.out.println(Arrays.toString(primitiveOutput));
        return primitiveOutput;
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        return new ArbitratorController(arbitrator, repo.deepCopy(), mapFun);
    }
}
