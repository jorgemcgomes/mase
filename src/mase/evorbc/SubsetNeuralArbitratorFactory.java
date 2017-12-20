/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.evorbc.Repertoire.Primitive;
import mase.neat.NEATAgentController;
import mase.neat.NEATSubpop;
import mase.util.FormatUtils;
import org.apache.commons.collections4.IteratorUtils;

/**
 *
 * @author jorge
 */
public class SubsetNeuralArbitratorFactory extends ArbitratorFactory {

    private static final long serialVersionUID = 1L;
    public static final String P_NUM_PRIMITIVES = "num-primitives";
    public static final String P_IGNORE_COORDINATES = "ignore-coordinates";
    protected int numPrimitives;
    protected int dimensions;
    protected boolean ignoreCoordinates;
    protected Primitive[] primitives;
    protected String P_PRIMITIVES = "primitives";
    protected String V_AUTO = "auto";
    //public static DescriptiveStatistics ds = new DescriptiveStatistics();

    @Override
    /**
     * Only works with NEAT
     */
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String str = state.parameters.getStringWithDefault(base.push(P_PRIMITIVES), DEFAULT_BASE.push(P_PRIMITIVES), V_AUTO);
        if (!str.equalsIgnoreCase(V_AUTO)) {
            state.output.warning("Primitives specified. Parameters " + P_NUM_PRIMITIVES + " and " + P_IGNORE_COORDINATES + " ignored.",
                    base.push(P_PRIMITIVES), DEFAULT_BASE.push(P_PRIMITIVES));
            int[] ids = FormatUtils.parseIntArray(str);
            if (ids.length < 2) {
                state.output.fatal("Less than 2 primitives (" + ids.length + ")", base.push(P_PRIMITIVES), DEFAULT_BASE.push(P_PRIMITIVES));
            }
            primitives = new Primitive[ids.length];
            for (int i = 0; i < ids.length; i++) {
                // TODO: the cast is a dirty but efficient hack. otherwise, just look for it in the collection of all primitives instead
                Primitive p = ((KdTreeRepertoire) repo).getPrimitiveById(ids[i]);
                if (p == null) {
                    state.output.fatal("Unknown primitive: " + ids[i]);
                }
                primitives[i] = p;
            }
            Parameter pOut = new Parameter(NEATSubpop.P_NEAT_BASE).push("OUTPUT.NODES");
            state.output.message("Forcing " + pOut + " to: " + primitives.length);
            state.parameters.set(pOut, primitives.length + "");
        } else {
            numPrimitives = state.parameters.getInt(base.push(P_NUM_PRIMITIVES), DEFAULT_BASE.push(P_NUM_PRIMITIVES));
            Parameter pOut = new Parameter(NEATSubpop.P_NEAT_BASE).push("OUTPUT.NODES");
            state.output.message("Forcing " + pOut + " to: " + numPrimitives);
            state.parameters.set(pOut, numPrimitives + "");

            ignoreCoordinates = state.parameters.getBoolean(base.push(P_IGNORE_COORDINATES), DEFAULT_BASE.push(P_IGNORE_COORDINATES), false);

            dimensions = ignoreCoordinates ? 1 : repo.allPrimitives().iterator().next().coordinates.length;
            Parameter pFeatures = new Parameter(NEATSubpop.P_NEAT_BASE).push("EXTRA.FEATURE.COUNT");
            state.output.message("Forcing " + pFeatures + " to: " + dimensions * numPrimitives);
            state.parameters.set(pFeatures, dimensions * numPrimitives + "");
        }
    }

    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        AgentControllerIndividual aci = (AgentControllerIndividual) inds[0];
        NEATAgentController arbitrator = (NEATAgentController) aci.decodeController();

        if (primitives == null) {
            double[] genes = arbitrator.getExtraGenes();
            Primitive[] prims = new Primitive[numPrimitives];
            for (int i = 0; i < prims.length; i++) {
                if (ignoreCoordinates) {
                    int index = (int) (genes[i] * (repo.allPrimitives().size() - 1));
                    prims[i] = IteratorUtils.get(repo.allPrimitives().iterator(), index);
                } else {
                    double[] subArray = new double[dimensions];
                    System.arraycopy(genes, i * dimensions, subArray, 0, dimensions);
                    double[] coords = mapFun.outputToCoordinates(subArray);
                    prims[i] = repo.nearest(coords).clone();
                }
            }
            SubsetNeuralArbitratorController ac = new SubsetNeuralArbitratorController(arbitrator, prims);
            return new HomogeneousGroupController(ac);
        } else {
            Primitive[] copy = new Primitive[primitives.length];
            for(int i = 0 ; i < copy.length ; i++) {
                copy[i] = primitives[i].clone();
            }
            SubsetNeuralArbitratorController ac = new SubsetNeuralArbitratorController(arbitrator, copy);
            return new HomogeneousGroupController(ac);
        }
    }
}
