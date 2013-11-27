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
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.EvaluationResult;
import mase.GroupController;
import mase.MetaEvaluator;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static final String P_CONTROLLER = "-gc";

    public static void main(String[] args) {
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
        GroupController controller = loadController(gc, true);
        Random rand = new Random();
        long startSeed = rand.nextLong();

        GUIState gui = sim.createSimStateWithUI(controller, startSeed);
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

    public static GroupController loadController(File gc, boolean verbose) {
        GroupController controller = null;
        try {
            FileInputStream fis = new FileInputStream(gc);
            ObjectInputStream ois = new ObjectInputStream(fis);
            controller = (GroupController) ois.readObject();
            System.out.println(controller);
            if (verbose) {
                EvaluationResult[] chars = (EvaluationResult[]) ois.readObject();
                for (int i = 0; i < chars.length; i++) {
                    System.out.println("Evaluation " + i + ":\n" + chars[i].toString() + "\n");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MasonPlayer.class.getName()).log(Level.FINE, null, ex);
        }
        return controller;
    }
}
