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
import java.util.ArrayList;
import java.util.Random;
import mase.MetaEvaluator;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static final String P_CONTROLLER = "-gc";
    public static final String P_AGENT_CONTROLLER = "-c";

    public static void main(String[] args) throws Exception {
        GroupController controller = createController(args);
        long startSeed = 0;
        MasonSimulator sim = createSimulator(args);
        GUIState gui = sim.createSimStateWithUI(controller, startSeed);
        gui.createController();
    }
    
    public static GroupController createController(String[] args) throws Exception {
                // Parameter loading
        File gc = null;
        ArrayList<File> controllers = new ArrayList<File>();
        int x;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(P_CONTROLLER)) {
                gc = new File(args[1 + x++]);
            } else if (args[x].equals(P_AGENT_CONTROLLER)) {
                controllers.add(new File(args[1 + x++]));
            }
        }
        if (gc == null && controllers.isEmpty()) {
            System.out.println("No controllers to run.");
            return null;
        }
        if (gc != null && !controllers.isEmpty()) {
            System.out.println("Both agent controllers and a group controller were provided.");
            return null;
        }

        // Create controller
        GroupController controller = null;
        if (gc != null) {
            PersistentSolution c = SolutionPersistence.readSolution(new FileInputStream(gc));
            System.out.println(c);
            controller = c.getController();
        } else {
            AgentController[] acs = new AgentController[controllers.size()];
            for(int i = 0 ; i < controllers.size() ; i++) {
                PersistentSolution c = SolutionPersistence.readSolution(new FileInputStream(controllers.get(i)));
                System.out.println("------------- Controller " + i + " -------------");
                System.out.println(c);
                HeterogeneousGroupController hgc = (HeterogeneousGroupController) c.getController();
                acs[i] = hgc.getAgentControllers(controllers.size())[i];
            }
            controller = new HeterogeneousGroupController(acs);
        }
        return controller;
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
