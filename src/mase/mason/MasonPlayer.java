/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import mase.MetaEvaluator;
import mase.stat.PersistentController;
import mase.stat.SolutionWriterStat;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static final String P_CONTROLLER = "-gc";

    public static void main(String[] args) throws Exception {
        // Parameter loading
        File gc = null;
        int x;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(P_CONTROLLER)) {
                gc = new File(args[1 + x++]);
            }
        }
        if (gc == null) {
            System.out.println("Missing argument -gc.");
            return;
        }
        if (!gc.exists()) {
            System.out.println("File does not exist: " + gc.getAbsolutePath());
            return;
        }

        // Create controller
        MasonSimulator sim = createSimulator(args);
        PersistentController c = SolutionWriterStat.readSolution(new FileInputStream(gc));
        System.out.println(c);
        long startSeed = new Random().nextLong();
        GUIState gui = sim.createSimStateWithUI(c.getController(), startSeed);
        gui.createController();
    }

    public static MasonSimulator createSimulator(String[] args) {
        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        Parameter base = new Parameter(EvolutionState.P_EVALUATOR).push(Evaluator.P_PROBLEM);
        if (state.parameters.exists(base, null)) {
            MasonSimulator sim = (MasonSimulator) db.getInstanceForParameter(base, null, MasonSimulator.class);
            sim.setup(state, base);
            return sim;
        } else {
            base = new Parameter(EvolutionState.P_EVALUATOR).push(MetaEvaluator.P_BASE_EVAL).push(Evaluator.P_PROBLEM);
            MasonSimulator sim = (MasonSimulator) db.getInstanceForParameter(base, null, MasonSimulator.class);
            sim.setup(state, base);
            return sim;
        }

    }
}
