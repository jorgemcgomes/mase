/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.controllers.GroupController;
import mase.stat.Reevaluate;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static void main(String[] args) throws Exception {
        GroupController controller = Reevaluate.createController(args);
        long startSeed = 0;
        MasonSimulator sim = (MasonSimulator) Reevaluate.createSimulator(args);
        GUIState gui = sim.createSimStateWithUI(controller, startSeed);
        gui.createController();
    }
}
