/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class MultiPopStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    public int log = 0;  // stdout by default

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //To change body of generated methods, choose Tools | Templates.
        File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);
        if (statisticsFile != null) {
            try {
                log = state.output.addLog(statisticsFile, true, false);
            } catch (IOException i) {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
    }

    @Override
    public void postPostBreedingExchangeStatistics(EvolutionState state) {
        super.postPreBreedingExchangeStatistics(state);
        SelectionPoolBuilder spb = (SelectionPoolBuilder) state.exchanger;
        state.output.print(state.generation + "", log);
        for(int i =  0 ; i < state.population.subpops.length ; i++) {
            state.output.print(" " + spb.getPool(i).length + " " + 
                    spb.dispersion[i] + " " + spb.selfPicked[i], log);
        }
        state.output.println("", log);
        
        // stats
        Arrays.fill(spb.selfPicked, 0);
    }
    
    
}
