/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.util.Arrays;
import mase.controllers.AgentController;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class ArbitratorController implements AgentController {

    private static final long serialVersionUID = 1L;

    private AgentController arbitrator;
    private Repertoire repo;
    private MappingFunction mapFun;
    private final boolean locking;
    public transient AgentController lastPrimitive = null;
    public transient int lastPrimitiveId;
    public transient double[] lastArbitratorOutput;
    public transient double[] lastRepertoireCoords;
    public transient boolean locked;

    public ArbitratorController() {
        this(null, null, null, false);
    }

    public ArbitratorController(AgentController arbitrator, Repertoire repo, MappingFunction fun, boolean locking) {
        this.arbitrator = arbitrator;
        this.repo = repo;
        this.mapFun = fun;
        this.locking = locking;
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
        lastArbitratorOutput = arbitrator.processInputs(input);
        double[] output = lastArbitratorOutput;
        locked = false;
        if (locking) {
            locked = output[0] < 0.5 && lastPrimitive != null;
            output = Arrays.copyOfRange(output, 1, output.length);
        }        
        if (!locked) {
            lastRepertoireCoords = mapFun.outputToCoordinates(output);

            Pair<Integer, AgentController> primitive = repo.nearest(lastRepertoireCoords);
            lastPrimitiveId = primitive.getLeft();
            if (lastPrimitive == null || primitive != lastPrimitive) {
                primitive.getRight().reset();
                lastPrimitive = primitive.getRight();
            }
        }

        double[] out = lastPrimitive.processInputs(input);

        return out;
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        return new ArbitratorController(arbitrator.clone(), repo.deepCopy(), mapFun, locking);
    }
}
