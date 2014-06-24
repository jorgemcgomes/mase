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
import java.text.DecimalFormat;
import mase.spec.HybridExchanger.MetaPopulation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class HybridStat extends Statistics {

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
    public void postPreBreedingExchangeStatistics(EvolutionState state) {
        super.postPreBreedingExchangeStatistics(state);
        HybridExchanger exc = (HybridExchanger) state.exchanger;
        state.output.print(state.generation + " " + exc.metaPops.size(), log);

        // metapop size (min, mean, max)
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.agents.size());
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // metapop dispersion (min, mean, max)
        ds.clear();
        for (int i = 0; i < exc.distanceMatrix.length; i++) {
            ds.addValue(exc.distanceMatrix[i][i]);
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // metapop difference to others
        ds.clear();
        for (int i = 0; i < exc.distanceMatrix.length; i++) {
            for (int j = i + 1; j < exc.distanceMatrix.length; j++) {
                ds.addValue(exc.distanceMatrix[i][j]);
            }
        }
        if (ds.getN() == 0) {
            ds.addValue(0);
        }
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // total number of merges and splits
        int mergeCount = 0, splitCount = 0;
        for (MetaPopulation mp : exc.metaPops) {
            if (mp.age == 0 && mp.agents.size() == 1) {
                splitCount++;
            } else if (mp.age == 0) {
                mergeCount++;
            }
        }
        state.output.println(" " + mergeCount + " " + splitCount, log);

        printMatrix(exc.distanceMatrix, state);
    }

    private void printMatrix(double[][] m, EvolutionState state) {
        DecimalFormat df = new DecimalFormat("0.000");
        try {
            StringBuilder sb = new StringBuilder();
            int rows = m.length;
            int columns = m[0].length;
            

            for (int i = 0; i < rows; i++) {
                sb.append("| ");
                for (int j = 0; j < columns; j++) {
                    sb.append(df.format(m[i][j]) + " ");
                }
                sb.append("|\n");
            }
            state.output.message(sb.toString());
        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

}
