/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.EvaluationResult;
import mase.GroupController;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static final String P_CONTROLLER = "-gc";

    public static void main(String[] args) {
        File gc = null;
        int x;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(P_CONTROLLER)) {
                gc = new File(args[x + 1]);
                args[x] = null;
                args[x + 1] = null;
                x++;
            }
        }
        if (gc == null) {
            System.out.println("Missing argument -gc.");
        }
        List<String> list = new ArrayList<String>(Arrays.asList(args));
        list.removeAll(Collections.singleton(null));
        String[] parArgs = list.toArray(new String[list.size()]);
        MasonSimulator sim = createSimulator(parArgs);
        GroupController controller = loadController(gc);
        GUIState gui = sim.createSimStateWithUI(controller, 0);
        gui.createController();
    }

    public static MasonSimulator createSimulator(String[] args) {
        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        state.setup(state, null);
        MasonSimulator sim = (MasonSimulator) state.evaluator.p_problem;
        return sim;
    }

    public static GroupController loadController(File gc) {
        GroupController controller = null;
        try {
            FileInputStream fis = new FileInputStream(gc);
            ObjectInputStream ois = new ObjectInputStream(fis);
            controller = (GroupController) ois.readObject();
            //System.out.println("Group Controller:\n" + controller.toString());
            EvaluationResult[] chars = (EvaluationResult[]) ois.readObject();
            for (int i = 0; i < chars.length; i++) {
                System.out.println("Evaluation " + i + ":\n" + chars[i].toString() + "\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(MasonPlayer.class.getName()).log(Level.FINE, null, ex);
        }
        return controller;
    }
}
