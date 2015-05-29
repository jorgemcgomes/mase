/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.deprecated;

import java.util.ArrayList;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class LeapTest {
    
    public static void main(String[] args) {
        double[] probs = new double[]{0.50, 0.40, 0.30, 0.20, 0.10, 0.05, 0.02, 0.01};
        for(double p : probs) {
            stats(p, 1000);
        }
    }
    
    static void stats(double changeProb, int reps) {
        double avg = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        ArrayList<Integer> counts = new ArrayList<Integer>(reps);
        for(int i = 0 ; i < reps ; i++) {
            int c = countRounds(changeProb);
            avg += c;
            max = Math.max(max, c);
            min = Math.min(min, c);
            counts.add(c);
        }
        avg = avg / reps;
        
        double stdev = 0;
        for(Integer c : counts) {
            stdev += Math.pow(c - avg, 2);
        }
        stdev = Math.sqrt(stdev / reps);
        System.out.println(changeProb + "\t" + min + "\t" + avg + "\t" + max + "\t" + stdev);
    }
    
    static int countRounds(double changeProb) {
        int count=0;
        while(Math.random() > changeProb) {
            count++;
        }
        return count;
    }
    
}
