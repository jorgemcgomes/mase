/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.gp.GPNode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import mase.evaluation.EvaluationResult;

/**
 *
 * @author jorge
 */
public class NodeSetResult implements EvaluationResult<Set<GPNode>> {

    private static final long serialVersionUID = 1L;
    
    private final Set<GPNode> usedNodes;
    
    public NodeSetResult(Set<GPNode> used) {
        this.usedNodes = used;
    }

    @Override
    public Set<GPNode> value() {
        return usedNodes;
    }

    @Override
    public EvaluationResult<Set<GPNode>> mergeEvaluations(Collection<EvaluationResult<Set<GPNode>>> results) {
        Set<GPNode> allUsed = new HashSet<>();
        for(EvaluationResult<Set<GPNode>> e : results) {
            allUsed.addAll(e.value());
        }
        return new NodeSetResult(allUsed);
    }

    @Override
    public String toString() {
        return usedNodes.size() + "";
    }
    
    
}
