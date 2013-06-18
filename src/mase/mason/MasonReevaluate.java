/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import mase.EvaluationResult;
import mase.GroupController;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonReevaluate {

    public static final String P_NREPS = "-n";

    public static void main(String[] args) {
        File gc = null;
        int x;
        int nreps = 0;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(MasonPlayer.P_CONTROLLER)) {
                gc = new File(args[x + 1]);
                args[x] = null;
                args[x + 1] = null;
                x++;
            } else if (args[x].equals(P_NREPS)) {
                nreps = Integer.parseInt(args[x + 1]);
                args[x] = null;
                args[x + 1] = null;
                x++;
            }
        }
        if (nreps <= 0 || gc == null || !gc.exists()) {
            System.out.println("Wrong or missing arguments.");
            System.exit(1);
        }
        List<String> list = new ArrayList<String>(Arrays.asList(args));
        list.removeAll(Collections.singleton(null));
        String[] parArgs = list.toArray(new String[list.size()]);

        MasonSimulator sim = MasonPlayer.createSimulator(parArgs);
        GroupController controller = MasonPlayer.loadController(gc);

        Random rand = new Random();
        long startSeed = rand.nextLong();
        ArrayList<EvaluationResult[]> results = new ArrayList<EvaluationResult[]>(nreps);
        long start = System.currentTimeMillis();
        for (int i = 0; i < nreps; i++) {
            results.add(sim.evaluateSolution(controller, startSeed + i));
        }
        long end = System.currentTimeMillis();

        System.out.println("Total time: " + (end - start) + " | Time per eval: " + (end - start) / (double) nreps);

        // merge fitness
        float stdev = 0;
        float mean = 0;
        for(EvaluationResult[] r : results) {
            float fit = (Float) r[0].value();
            mean += fit;
        }

        // merge behavs
        EvaluationResult[] merged = new EvaluationResult[results.get(0).length];
        for (int i = 0; i < merged.length; i++) {
            EvaluationResult[] samples = new EvaluationResult[results.size()];
            for (int j = 0; j < samples.length; j++) {
                samples[j] = results.get(j)[i];
            }
            merged[i] = samples[0].mergeEvaluations(samples);
        }

        // print
        for (int i = 0; i < merged.length; i++) {
            System.out.println("Evaluation " + i + ":\n" + merged[i].toString() + "\n");
        }
    }
}
