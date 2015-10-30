package mase;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class FastMathTest {

    public static void main(String[] args) {
        final double from = -1;
        final double to = 1;
        final int steps = 1000;
        
        // init
        double b = FastMath.sqrt(2.5);
        
        double[] testValues = new double[steps];
        for(int i = 0 ; i < testValues.length ; i++) {
            testValues[i] = from + (to - from) * (i / (double) steps);
        }

        double[] normal = new double[testValues.length];
        long t = System.nanoTime();
        for (int x = 0; x < testValues.length; x++) {
            normal[x] = tanh(testValues[x]);  // BASELINE OPERATION
        }
        long tNormal = System.nanoTime() - t;

        double[] quick = new double[testValues.length];
        t = System.nanoTime();
        for (int x = 0; x < testValues.length; x++) {
            quick[x] = tanhQuick(testValues[x]); // OPERATION TO COMPARE
        }
        long tQuick = System.nanoTime() - t;
        
        double meanError = 0;
        double maxError = 0;
        for(int i = 0 ; i < testValues.length ; i++) {
            System.out.println(normal[i] + "\t" + quick[i]);
            double e = Math.abs(normal[i] - quick[i]);
            meanError += e; 
            maxError = Math.max(maxError, e);
        }
        
        System.out.println("Normal time: " + tNormal);
        System.out.println("Quick time:  " + tQuick);
        System.out.println("Mean error:  " + meanError / testValues.length);
        System.out.println("Max error:   " + maxError);

        
    }
    
    private static double tanh(double v) {
        return (1 - FastMath.exp(-2 * v)) / (1 + FastMath.exp(-2 * v));
    }
    
    private static double tanhQuick(double v) {
        return (1 - FastMath.expQuick(-2 * v)) / (1 + FastMath.expQuick(-2 * v));
    }

}
