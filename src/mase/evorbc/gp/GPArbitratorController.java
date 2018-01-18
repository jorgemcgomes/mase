/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.gp.GPNode;
import ec.gp.GPTree;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import mase.controllers.AgentController;
import mase.evorbc.KdTreeRepertoire;
import mase.evorbc.Repertoire;
import mase.evorbc.Repertoire.Primitive;
import mase.evorbc.RepertoireInspector;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 *
 * @author jorge
 */
public class GPArbitratorController implements AgentController, ProvidesInspector {

    private static final long serialVersionUID = 1L;
    private final GPTree tree;
    private final Repertoire repo;
    private transient Primitive lastPrimitive;
    private transient GPNode lastSelectedNode;

    public GPArbitratorController(GPTree tree, Repertoire repo) {
        this.tree = tree;
        this.repo = repo;
    }

    public Primitive getLastPrimitive() {
        return lastPrimitive;
    }

    @Override
    public double[] processInputs(double[] input) {
        // 1. get the primitive from the GP tree
        Data d = new Data();
        d.sensorValues = input;
        d.ac = this;
        tree.child.eval(null, 0, d, null, null, null);
        lastSelectedNode = d.selected;
        if (lastPrimitive == null || lastPrimitive.id != d.primitive) {
            KdTreeRepertoire kdrep = (KdTreeRepertoire) repo;
            Primitive p = kdrep.getPrimitiveById(d.primitive); // grab the primitive from the given repo (cloned)
            if (p != null) { // Primitive found, change
                lastPrimitive = p;
                lastPrimitive.ac.reset();
            } // else, primitive not found, stay with the same as before
            //System.out.println(lastPrimitive.id);
        }

        // 2. execute the primitive
        if (lastPrimitive != null) {
            return lastPrimitive.ac.processInputs(input);
        } else {
            return null;
        }
    }

    @Override
    public void reset() {
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        return new GPArbitratorController(tree/*(GPTree) tree.clone()*/, repo.deepCopy());
    }

    public GPTree getProgramTree() {
        return tree;
    }

    public GPNode getLastSelectedNode() {
        return lastSelectedNode;
    }

    public Repertoire getRepertoire() {
        return repo;
    }

    @Override
    public String toString() {
        //return tree.child.makeCTree(true, true, true) + "\n" + repo.toString();
        //return tree.child.makeGraphvizTree() + "\nSize: " + tree.child.numNodes(GPNode.NODESEARCH_ALL) +" Depth: " + tree.child.depth() + "\n" + repo.toString();
        return tree.child.makeGraphvizTree() + "\n\n" + tree.child.makeCTree(true, true, true) + "\nSize: " + tree.child.numNodes(GPNode.NODESEARCH_ALL) + " Depth: " + tree.child.depth() + "\n" + repo.toString();

    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        // Get all primitives in the tree
        HashSet<Integer> ids = new HashSet<>();
        Iterator iter = tree.child.iterator();
        while (iter.hasNext()) {
            GPNode node = (GPNode) iter.next();
            if (node instanceof RepoPrimitive) {
                ids.add(((RepoPrimitive) node).getPrimitive());
            }
        }
        final List<Primitive> treePrimitives = new ArrayList<>();
        for (Primitive p : repo.allPrimitives()) {
            if (ids.contains(p.id)) {
                treePrimitives.add(p);
            }
        }
        final Collection<Primitive> nonused = new ArrayList<>(repo.allPrimitives());
        nonused.removeAll(treePrimitives);

        RepertoireInspector insp = new RepertoireInspector(400, 7) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawAxis(g);
                drawPrimitives(nonused, g);
                drawPrimitives(treePrimitives, Color.GREEN, g);
                if (lastPrimitive != null) {
                    highlightPrimitive(lastPrimitive, g);
                }
            }
        };
        insp.setBounds(repo.allPrimitives());
        return insp;
    }

}
