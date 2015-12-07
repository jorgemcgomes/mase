/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Checkpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author jorge
 */
public class MaseResume {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String checkpoint = null;
        HashMap<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(Evolve.A_CHECKPOINT)) {
                checkpoint = args[i + 1];
                i++;
            } else if (args[i].equals("-p")) {
                String[] a = args[i + 1].split("=");
                params.put(a[0], a[1]);
            }
        }

        if (checkpoint == null) {
            System.out.println("Required parameter not specified: " + Evolve.A_CHECKPOINT);
            return;
        }
        
        EvolutionState s = Checkpoint.restoreFromCheckpoint(checkpoint);
        for(Entry<String,String> e : params.entrySet()) {
            if(e.getKey().equals(EvolutionState.P_GENERATIONS)) {
                int generations = Integer.parseInt(e.getValue());
                s.numGenerations = generations;
            } else if(e.getKey().equals(EvolutionState.P_EVALUATIONS)) {
                long evaluations = Long.parseLong(e.getValue());
                s.numEvaluations = evaluations;
            }
        }

        s.run(EvolutionState.C_STARTED_FROM_CHECKPOINT);
        Evolve.cleanup(s);
        System.exit(0);
    }

}
