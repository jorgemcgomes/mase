/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import mase.controllers.AgentController;
import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;

/**
 *
 * @author jorge
 */
public class ArbitratorController implements AgentController {

    private static final long serialVersionUID = 1L;

    private AgentController arbitrator;
    private KDTree<AgentController> repo;
    private AgentController lastPrimitive = null;

    public ArbitratorController() {
        this(null, null);
    }

    public ArbitratorController(AgentController arbitrator, KDTree<AgentController> repo) {
        this.arbitrator = arbitrator;
        this.repo = repo;
    }

    /**
     * Assumes that the values of the keys are in the range [0,1]
     *
     * @param repo
     */
    public void setRepertoire(KDTree<AgentController> repo) {
        this.repo = repo;
    }

    public void setArbitrator(AgentController arbitrator) {
        this.arbitrator = arbitrator;
    }

    @Override
    public double[] processInputs(double[] input) {
        double[] arbitratorOutput = arbitrator.processInputs(input);

        // TODO: add fancy mapping support
        Neighbor<double[], AgentController> nearest = repo.nearest(arbitratorOutput);
        
        /*for(double d : nearest.key) {
            System.out.print(d + " ");
        }
        System.out.println();*/
        
        AgentController primitive = nearest.value;
        if (lastPrimitive == null || primitive != lastPrimitive) {
            primitive.reset();
            lastPrimitive = primitive;
        }
        double[] primitiveOutput = primitive.processInputs(input);
        return primitiveOutput;
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        return new ArbitratorController(arbitrator, repo);
    }
}
