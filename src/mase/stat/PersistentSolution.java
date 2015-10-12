/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.stat;

import java.io.Serializable;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;

/**
 *
 * @author jorge
 */
public class PersistentSolution implements Serializable, Comparable<PersistentSolution> {
    
    private static final long serialVersionUID = 1;
    
    private GroupController controller;
    private EvaluationResult[] evalResults;
    private double fitness;
    private int generation;
    private int subpop;
    private int index;
    private Serializable userData;

    public GroupController getController() {
        return controller;
    }

    public void setController(GroupController controller) {
        this.controller = controller;
    }

    public EvaluationResult[] getEvalResults() {
        return evalResults;
    }

    public void setEvalResults(EvaluationResult[] evalResults) {
        this.evalResults = evalResults;
    }
    
    public void setOrigin(int generation, int subpop, int index) {
        this.generation = generation;
        this.subpop = subpop;
        this.index = index;
    }

    public int getGeneration() {
        return generation;
    }

    public int getSubpop() {
        return subpop;
    }

    public int getIndex() {
        return index;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Serializable userData) {
        this.userData = userData;
    }
    
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    public double getFitness() {
        return fitness;
    }
    @Override
    public int compareTo(PersistentSolution o) {
        int genC = Integer.compare(this.generation, o.generation);
        if(genC != 0) {
            return genC;
        }
        int subC = Integer.compare(this.subpop, o.subpop);
        if(subC != 0) {
            return subC;
        }
        int indC = Integer.compare(this.index, o.index);
        if(indC != 0) {
            return indC;
        }
        return 0;
    }

    @Override
    public String toString() {
        String str = "Generation: " + generation + "; Subpop: " + subpop + "; Index: " + index + "\n\n";
        str += controller.toString() + "\n";
        int i = 0;
        for(EvaluationResult er : evalResults) {
            str += "Evaluation " + (i++) + "\n";
            str += er.toString();
        }
        str += "\nFitness score: " + fitness + "\n";
        if(userData != null) {
            str += "User data:\n" + userData;
        }
        return str + "\n";
    }
}
