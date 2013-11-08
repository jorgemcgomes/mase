/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SelectionBenchmark {

    public static void main(String[] args) {

        int L = 100, N = 10000;
        double[] truncationP = new double[]{0.25, 0.50, 0.75};
        int[] tournamentP = new int[]{2, 5, 7, 10};

        DescriptiveStatistics[] truncationStat = new DescriptiveStatistics[truncationP.length];
        for (int i = 0; i < truncationStat.length; i++) {
            truncationStat[i] = new DescriptiveStatistics();
        }
        DescriptiveStatistics[] tournamentStat = new DescriptiveStatistics[tournamentP.length];
        DescriptiveStatistics[] tournamentStat2 = new DescriptiveStatistics[tournamentP.length];
        for (int i = 0; i < tournamentStat.length; i++) {
            tournamentStat[i] = new DescriptiveStatistics();
            tournamentStat2[i] = new DescriptiveStatistics();
        }
        DescriptiveStatistics rouletteStat = new DescriptiveStatistics();
        DescriptiveStatistics rouletteStat2 = new DescriptiveStatistics();
        DescriptiveStatistics baseStat = new DescriptiveStatistics();

        for (int i = 0; i < N; i++) {
            // generate test vector
            double[] test = new double[L];
            for (int j = 0; j < L; j++) {
                test[j] = Math.random();
            }

            // truncation
            for (int p = 0; p < truncationP.length; p++) {
                double[] v = Arrays.copyOf(test, test.length);
                Arrays.sort(v);
                int nElites = (int) Math.ceil(truncationP[p] * test.length);
                double cutoff = v[test.length - nElites];
                double[] weights = new double[test.length];
                for (int k = 0; k < test.length; k++) {
                    weights[k] = test[k] >= cutoff ? test[k] * (1 / truncationP[p]) : 0;
                }
                truncationStat[p].addValue(sum(weights));
            }

            // tournament
            for (int p = 0; p < tournamentP.length; p++) {
                double[] weights = new double[test.length];
                HashSet<Integer> added = new HashSet<Integer>();
                for (int k = 0; k < test.length; k++) {
                    int idx = makeTournament(test, tournamentP[p]);
                    weights[idx] += test[idx];
                    added.add(idx);
                }
                tournamentStat2[p].addValue(added.size());
                tournamentStat[p].addValue(sum(weights));
            }

            // roulette
            double[] weights = new double[test.length];
            HashSet<Integer> added = new HashSet<Integer>();
            for (int k = 0; k < test.length; k++) {
                int idx = roulette(test);
                weights[idx] += test[idx];
                added.add(idx);
            }
            rouletteStat.addValue(sum(weights));
            rouletteStat2.addValue(added.size());
            
            // base
            baseStat.addValue(sum(test));
        }

        for (int p = 0; p < truncationP.length; p++) {
            System.out.println("Truncation\t" + truncationP[p] + "\t" + truncationStat[p].getMean() + "\t" + truncationStat[p].getStandardDeviation() + "\t" + ((int)Math.ceil(L * truncationP[p])) + "\t 0");
        }
        for (int p = 0; p < tournamentP.length; p++) {
            System.out.println("Tournament\t" + tournamentP[p] + "\t" + tournamentStat[p].getMean() + "\t" + tournamentStat[p].getStandardDeviation() + "\t" + tournamentStat2[p].getMean() + "\t" + tournamentStat2[p].getStandardDeviation());
        }
        System.out.println("Roulette\t\t" + rouletteStat.getMean() + "\t" + rouletteStat.getStandardDeviation() + "\t" + rouletteStat2.getMean() + "\t" + rouletteStat2.getStandardDeviation());
        System.out.println("Base    \t\t" + baseStat.getMean() + "\t" + baseStat.getStandardDeviation() + "\t " + L + "\t0");
    }

    private static int makeTournament(double[] weights, int k) {
        int[] players = new int[k];
        for(int i = 0 ; i < k ; i++) {
            players[i] = (int) (Math.random() * weights.length);
        }
        int best = 0;
        for(int i = 1 ; i < k ; i++) {
            if(weights[players[i]] > weights[best]) {
                best = players[i];
            }
        }
        return best;
    }

    private static int roulette(double[] weights) {
        double sum = sum(weights);
        
        double decision = Math.random();
        double s = 0;
        for(int i = 0 ; i < weights.length ; i++) {
            s += weights[i] / sum;
            if(decision <= s) {
                return i;
            }
        }
        return -1;
    }

    private static double sum(double[] weights) {
        double s = 0;
        for (double w : weights) {
            s += w;
        }
        return s;
    }

}
