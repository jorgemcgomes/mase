/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import mase.controllers.GroupController;
import mase.stat.Reevaluate;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static void main(String[] args) throws Exception {
        File parent = Reevaluate.findControllerFile(args).getParentFile();
        GroupController controller = Reevaluate.createController(args);
        long startSeed = 0;
        MasonSimulationProblem sim = (MasonSimulationProblem) Reevaluate.createSimulator(args, parent);
        GUIState gui = sim.createSimStateWithUI(controller, startSeed);
        gui.createController();
    }
}
