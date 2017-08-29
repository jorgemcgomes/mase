/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import mase.evaluation.ExpandedFitness;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jorge
 */
public class MEFinalRepertoireTextStat extends Statistics {

    public static final String P_FILE = "file";
    public static final String P_UPDATE_ALWAYS = "update-always";
    private static final long serialVersionUID = 1L;
    private boolean updateAlways;
    private File logFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        logFile = state.parameters.getFile(base.push(P_FILE), null);
        updateAlways = state.parameters.getBoolean(base.push(P_UPDATE_ALWAYS), null, false);
    }

    @Override
    public void preBreedingStatistics(EvolutionState state) {
        super.preBreedingStatistics(state);
        if (updateAlways) {
            logRepo(state, false);
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        logRepo(state, true);
    }

    private void logRepo(EvolutionState state, boolean print) {
        try {
            int log = state.output.addLog(logFile, true);
            MESubpopulation sub = (MESubpopulation) state.population.subpops.get(0);
            Collection<Entry<Integer, Individual>> entries = sub.map.entries();

            boolean headed = false;
            boolean twoDimensional = true;

            for (Entry<Integer, Individual> e : entries) {
                ExpandedFitness ef = (ExpandedFitness) e.getValue().fitness;
                double[] behav = sub.getBehaviourVector(state, e.getValue());
                int[] bin = sub.binFromHash(e.getKey());
                double[] genome = ((DoubleVectorIndividual) e.getValue()).genome;

                if (!headed) { // add file header
                    twoDimensional = bin.length == 2;
                    state.output.print("Hash Hits Fitness", log);
                    for (int i = 0; i < bin.length; i++) {
                        state.output.print(" Bin_" + i, log);
                    }
                    for (int i = 0; i < behav.length; i++) {
                        state.output.print(" Behav_" + i, log);
                    }
                    for (int i = 0; i < genome.length; i++) {
                        state.output.print(" Genome_" + i, log);
                    }
                    state.output.println("", log);
                    headed = true;
                }

                state.output.print(e.getKey() + " " + sub.numHits(e.getKey()) + " " + ef.getFitnessScore(), log);
                for (int b : bin) {
                    state.output.print(" " + b, log);
                }
                for (double v : behav) {
                    state.output.print(" " + v, log);
                }
                for (double g : genome) {
                    state.output.print(" " + g, log);
                }
                state.output.println("", log);
            }
            if (twoDimensional && print) {
                state.output.message(toString2D(sub));
            }
        } catch (IOException ex) {
            state.output.fatal("An IOException occurred while trying to create the log " + logFile);
        }
    }

    // Only works for 2D
    protected String toString2D(MESubpopulation sub) {
        StringBuilder sb = new StringBuilder();

        // find the bounds of the repertoire
        Collection<int[]> values = new ArrayList<>(sub.inverseHash.values());
        int min0 = Integer.MAX_VALUE, min1 = Integer.MAX_VALUE, max0 = Integer.MIN_VALUE, max1 = Integer.MIN_VALUE;
        for (int[] v : values) {
            min0 = Math.min(min0, v[0]);
            max0 = Math.max(max0, v[0]);
            min1 = Math.min(min1, v[1]);
            max1 = Math.max(max1, v[1]);
        }

        // print repertoire
        int pad = Math.max(Integer.toString(min1).length(), Integer.toString(max1).length());
        sb.append(StringUtils.repeat(' ', pad)).append("y\n");
        for (int i1 = max1; i1 >= min1; i1--) {
            sb.append(StringUtils.leftPad(i1 + "", pad)).append("|");
            for (int i0 = min0; i0 <= max0; i0++) {
                int h = sub.hash(new int[]{i0, i1});
                if (sub.map.containsKey(h)) {
                    sb.append("[]");
                } else {
                    sb.append("  ");
                }
            }
            sb.append("\n");
        }

        // x-axis lines
        sb.append(StringUtils.repeat(' ', pad)).append('-');
        for (int i0 = min0; i0 <= max0; i0++) {
            sb.append("--");
        }
        sb.append(" x\n");

        // x-axis numbers
        sb.append(StringUtils.repeat(' ', pad + 1));
        int longest = Math.max(Integer.toString(min0).length(), Integer.toString(max0).length());
        int space = (longest + 2) / 2 * 2;
        int order = 0;
        for (int i0 = min0; i0 <= max0; i0++) {
            String s = Integer.toString(i0);
            if (order % (space / 2) == 0) {
                sb.append(StringUtils.rightPad(s, space));
            }
            order++;
        }
        sb.append('\n');
        return sb.toString();
    }

}
