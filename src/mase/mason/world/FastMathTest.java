/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class FastMathTest {

    public static void main(String[] args) {
        final double INCREMENT = 0.001;
        // init
        double b = FastMath.sqrt(2.5);

        double[] normal = new double[10000000];
        double v = INCREMENT;
        long t = System.nanoTime();
        for (int x = 0; x < 10000000; x++) {
            normal[x] = FastMath.sqrt(v);
            //normal[x] = v * v;
            v += INCREMENT;
        }
        long tNormal = System.nanoTime() - t;

        double[] quick = new double[10000000];
        v = INCREMENT;
        t = System.nanoTime();
        for (int x = 0; x < 10000000; x++) {
            quick[x] = FastMath.sqrtQuick(v);
            //quick[x] = FastMath.pow2(v);
            v += INCREMENT;
        }
        long tQuick = System.nanoTime() - t;
        
        double meanError = 0;
        double maxError = 0;
        for(int i = 0 ; i < normal.length ; i++) {
            double e = Math.abs(normal[i] - quick[i]);
            meanError += e; 
            maxError = Math.max(maxError, e);
        }
        
        System.out.println("Normal time: " + tNormal);
        System.out.println("Quick time:  " + tQuick);
        System.out.println("Mean error:  " + meanError / normal.length);
        System.out.println("Max error:   " + maxError);

        
    }

}
