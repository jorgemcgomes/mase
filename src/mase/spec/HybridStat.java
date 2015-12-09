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
import mase.spec.AbstractHybridExchanger.Foreign;
import mase.spec.AbstractHybridExchanger.MetaPopulation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class HybridStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default
    private int selfIndividuals = 0;
    private int foreignIndividuals = 0;
    private int totalMerges = 0, totalSplits = 0, totalRemerges = 0;

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
        BasicHybridExchanger exc = (BasicHybridExchanger) state.exchanger;
        state.output.print(state.generation + " " + exc.metaPops.size(), log);

        int foreigns = 0, foreignInds = 0, metapopInds = 0;
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.agents.size());
            foreigns += mp.foreigns.size();
            for (Foreign f : mp.foreigns) {
                foreignInds += f.inds.length;
            }
            metapopInds += mp.inds.length;
        }
        // metapop size (min, mean, max) and foreign pops
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax() + " " + foreigns, log);

        // total number of self-individuals, foreign individuals, and total
        selfIndividuals += metapopInds;
        foreignIndividuals += foreignInds;
        state.output.print(" " + selfIndividuals + " " + foreignIndividuals + " " + (selfIndividuals + foreignIndividuals), log);

        // metapop age
        ds = new DescriptiveStatistics();
        for(MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.age);
        }
        state.output.print(" " + ds.getMean() + " " + ds.getMax(), log);
        
        // metapop dispersion (min, mean, max)
        ds.clear();
        for (int i = 0; i < exc.distanceMatrix.length; i++) {
            ds.addValue(exc.distanceMatrix[i][i]);
        }
        state.output.print(/*" " + ds.getMin() + */" " + ds.getMean()/* + " " + ds.getMax()*/, log);

        // metapop difference to others
        ds.clear();
        for (int i = 0; i < exc.distanceMatrix.length; i++) {
            for (int j = i + 1; j < exc.distanceMatrix.length; j++) {
                if (!Double.isInfinite(exc.distanceMatrix[i][j])) {
                    ds.addValue(exc.distanceMatrix[i][j]);
                }
            }
        }
        if (ds.getN() == 0) {
            ds.addValue(0);
        }
        state.output.print(/*" " + ds.getMin() + */" " + ds.getMean()/* + " " + ds.getMax()*/, log);

        // number of splits, merges and remerges in this generation
        state.output.print(" " + exc.merges + " " + exc.splits + " " + exc.remerges, log);

        // total number of splits, merges, remerges
        totalMerges += exc.merges;
        totalSplits += exc.splits;
        totalRemerges += exc.remerges;
        state.output.println(" " + totalMerges + " " + totalSplits + " " + totalRemerges, log);

        
        for(MetaPopulation mp : exc.metaPops) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%3d", mp.age)).append(" - ").append(mp.toString());
            if(!mp.foreigns.isEmpty()) {
                sb.append(" - Foreigns:");
            }
            for(Foreign f : mp.foreigns) {
                sb.append(" ").append(f.origin).append("(").append(f.age).append(")");
            }
            state.output.message(sb.toString());
        }
        
        printMatrix(exc.distanceMatrix, state);
    }

    private void printMatrix(double[][] m, EvolutionState state) {
        DecimalFormat df = new DecimalFormat("000.000");
        try {
            StringBuilder sb = new StringBuilder();
            int rows = m.length;
            int columns = m[0].length;
            for (int i = 0; i < rows; i++) {
                sb.append("| ");
                for (int j = 0; j < columns; j++) {
                    sb.append(Double.isInfinite(m[i][j]) ? "   \u221E   " : df.format(m[i][j])).append(" ");
                }
                sb.append("|\n");
            }
            state.output.message(sb.toString());
        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

}
