/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import mase.controllers.GroupController;
import mase.stat.ReevaluationTools;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static void main(String[] args) throws Exception {
        File parent = ReevaluationTools.findControllerFile(args).getParentFile();
        GroupController controller = ReevaluationTools.createController(args);
        long startSeed = 0;
        MasonSimulationProblem sim = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, parent);
        MasonSimState state = sim.getSimState(controller, startSeed);
        GUIState gui = sim.getSimStateUI(state);
        gui.createController();
    }
}
