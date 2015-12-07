/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import java.util.Random;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.stat.Reevaluate;

/**
 *
 * @author jorge
 */
public class NEATTest {

    public static void main(String[] args) throws Exception {
        GroupController gc = Reevaluate.createController(new String[]{"-gc", "job.0.last.ind"});
        AgentController[] acs = gc.getAgentControllers(2);
        AgentController aerial = acs[1].clone();

        int N = 15;
        int L = 1000;
        int O = 4;

        double[][] data = new double[L][N];
        double[][] output = new double[L][O];
        Random rand = new Random(0);

        for (int l = 0; l < L; l++) {
            for (int n = 0; n < N; n++) {
                data[l][n] = rand.nextDouble() * 2 - 1;
            }
        }

        long start = System.currentTimeMillis();
        for (int l = 0; l < L; l++) {
            output[l] = aerial.processInputs(data[l]);
        }
        long end = System.currentTimeMillis();

        for (int l = 0; l < L; l++) {
            for (int i = 0; i < O; i++) {
                System.out.print(output[l][i] + "\t");
            }
            System.out.println();
        }
        System.out.println(end - start);
    }

}
