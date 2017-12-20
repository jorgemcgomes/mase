/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Parameter;
import java.util.Set;
import mase.MaseProblem;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.PostEvaluator;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class CleanupCodePostEvaluator implements PostEvaluator {

    private static final long serialVersionUID = 1L;
    private int evalIndex = -1;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        MaseProblem prob = (MaseProblem) state.evaluator.p_problem;
        EvaluationFunction[] evalFunctions = prob.getEvalFunctions();
        for (int i = 0; i < evalFunctions.length && evalIndex == -1; i++) {
            if (evalFunctions[i] instanceof UsedNodesEvaluation) {
                evalIndex = i;
            }
        }
        if (evalIndex == -1) {
            state.output.warning("CleanupCodePostEvaluator: UsedNodesEvaluation not "
                    + "found in the array of EvaluationFunctions. Adding.");
            UsedNodesEvaluation e = new UsedNodesEvaluation();
            evalIndex = evalFunctions.length;
            evalFunctions = ArrayUtils.add(evalFunctions, e);
            prob.setEvalFunctions(evalFunctions);
        }
    }

    @Override
    public void processPopulation(EvolutionState state) {
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                GPIndividual gpi = (GPIndividual) ind;
                EvaluationResult res = ((ExpandedFitness) ind.fitness).getCorrespondingEvaluation(evalIndex);
                NodeSetResult ue = (NodeSetResult) res;
                Set<GPNode> used = ue.value();
                prune(gpi.trees[0].child, used);
            }
        }
    }
    
    public static void prune(GPNode node, Set<GPNode> used) {
        int currentSize;
        do {
            currentSize = node.numNodes(GPNode.NODESEARCH_ALL);
            pruneAux(node, used);
        } while(currentSize != node.numNodes(GPNode.NODESEARCH_ALL));
    }

    private static void pruneAux(GPNode node, Set<GPNode> used) {
        if (node instanceof IfElse || node instanceof SensorLower || node instanceof SensorBinary) {
            GPNode child1 = node instanceof SensorBinary ? node.children[0] : node.children[1];
            GPNode child2 = node instanceof SensorBinary ? node.children[1] : node.children[2];
            pruneAux(child1, used);
            pruneAux(child2, used);
            if (child1 instanceof RepoPrimitive && !used.contains(child1)) {
                // first primitive never used, replace with second
                replaceWith(node, child2);
            } else if (child2 instanceof RepoPrimitive && !used.contains(child2)) {
                // second primitive never used, replace with first
                replaceWith(node, child1);
            } else if (child1 instanceof RepoPrimitive && child2 instanceof RepoPrimitive) {
                // both are the same, replace with one
                if (child1.nodeEquals(child2)) {
                    replaceWith(node, child1);
                }
            }
        }
    }

    private static void replaceWith(GPNode oldNode, GPNode newNode) {
        newNode.parent = oldNode.parent;
        newNode.argposition = oldNode.argposition;
        // replace the parent pointer
        if (oldNode.parent instanceof GPNode) {
            ((GPNode) (oldNode.parent)).children[oldNode.argposition] = newNode;
        } else {
            ((GPTree) (oldNode.parent)).child = newNode;
        }
    }
}
