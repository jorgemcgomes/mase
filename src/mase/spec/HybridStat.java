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
import mase.evaluation.MetaEvaluator;
import mase.spec.AbstractHybridExchanger.MetaPopulation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Generation | Evaluations | Number of pops | Min|Mean|Max agents in pop | 
 * Mean|Max pop age | Number of Merges|Splits in generation | Total number of Merges|Splits
 * [Min|Mean|Max population distance]
 * @author jorge
 */
public class HybridStat extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    private static final long serialVersionUID = 1L;
    public int log = 0;  // stdout by default
    private int totalMerges = 0, totalSplits = 0;

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
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);
        // header
        state.output.print("Generation Evaluations Pops MinSize MeanSize MaxSize MeanAge MaxAge Merges Splits TotalMerges TotalSplits", log);
        if(state.exchanger instanceof StochasticHybridExchanger) {
            state.output.print(" PotentialMerges MinDistance MeanDistance MaxDistance", log);
        }
        state.output.println("", log);
    }
    
    @Override
    public void postPreBreedingExchangeStatistics(EvolutionState state) {
        super.postPreBreedingExchangeStatistics(state);
        AbstractHybridExchanger exc = (AbstractHybridExchanger) state.exchanger;
        MetaEvaluator me = (MetaEvaluator) state.evaluator;
        // generation, evaluations, and number of metapops
        state.output.print(state.generation + " " + me.totalEvaluations + " " + exc.metaPops.size(), log);

        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.agents.size());
        }
        // metapop size (min, mean, max)
        state.output.print(" " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);

        // metapop mean and max age
        ds.clear();
        for(MetaPopulation mp : exc.metaPops) {
            ds.addValue(mp.age);
        }
        state.output.print(" " + ds.getMean() + " " + ds.getMax(), log);
        
        // number of splits and merges in this generation + total number of splits and merges
        totalMerges += exc.merges;
        totalSplits += exc.splits;
        state.output.print(" " + exc.merges + " " + exc.splits + " " + totalMerges + " " + totalSplits, log);

        if(exc instanceof StochasticHybridExchanger) {
            StochasticHybridExchanger she = (StochasticHybridExchanger) exc;
            // metapop difference to others
            ds.clear();
            for (int i = 0; i < she.distanceMatrix.length; i++) {
                for (int j = i + 1; j < she.distanceMatrix.length; j++) {
                    if (!Double.isInfinite(she.distanceMatrix[i][j]) && !Double.isNaN(she.distanceMatrix[i][j])) {
                        ds.addValue(she.distanceMatrix[i][j]);
                    }
                }
            }
            if(ds.getN() > 0) {
                state.output.print(" " + ds.getN() + " " + ds.getMin() + " " + ds.getMean() + " " + ds.getMax(), log);
            } else {
                state.output.print(" 0 0 0 0", log);
            }
            
            //printMatrix(she.distanceMatrix, state);
        }
        
        state.output.println("", log);

        /*for(MetaPopulation mp : exc.metaPops) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%3d", mp.age)).append(" - ").append(mp.toString());
            if(!mp.foreigns.isEmpty()) {
                sb.append(" - Foreigns:");
            }
            for(Foreign f : mp.foreigns) {
                sb.append(" ").append(f.origin).append("(").append(f.age).append(")");
            }
            state.output.message(sb.toString());
        }*/
        
        /*for(MetaPopulation mp : exc.metaPops) {
            state.output.message(mp.age + "/" + mp.lockDown);
        }*/
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
                    sb.append(Double.isInfinite(m[i][j]) ? "   \u221E   " : Double.isNaN(m[i][j]) ? "  NaN  " : df.format(m[i][j])).append(" ");
                }
                sb.append("|\n");
            }
            state.output.message(sb.toString());
        } catch (Exception e) {
            state.output.message("Matrix is empty!!");
        }
    }

}
