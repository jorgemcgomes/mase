/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPTree;
import ec.util.Parameter;
import java.io.File;

/**
 *
 * @author jorge
 */
public class InitialPopulationDump extends Statistics {

    public static final String P_FILE = "file";
    public int log;
    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File f = state.parameters.getFile(base.push(P_FILE), null);
        try {
            log = state.output.addLog(f, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        Subpopulation subpop = state.population.subpops[0];
        int index = 0;
        for(Individual i : subpop.individuals) {
            GPIndividual gpi = (GPIndividual) i;
            state.output.println("---------------------------- " + index + " ----------------------------", log);
            gpi.trees[0].printStyle = GPTree.PRINT_STYLE_C;
            gpi.trees[0].printTerminalsAsVariablesInC = true;
            gpi.trees[0].printTwoArgumentNonterminalsAsOperatorsInC = true;
            gpi.trees[0].printTreeForHumans(state, log);
            index++;
        }

    }

}
