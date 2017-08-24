/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.util.List;
import mase.controllers.GroupController;
import mase.stat.PersistentSolution;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import sim.display.Controller;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonPlayer {

    public static void main(String[] args) throws Exception {
        File tar = ReevaluationTools.findControllerFile(args);
        if (tar == null) {
            System.err.println("Controller(s) not found! Use -gc");
            System.exit(1);
        }
        long startSeed = 0;
        File parent = ReevaluationTools.findControllerFile(args).getParentFile();
        MasonSimulationProblem sim = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, parent);

        if (tar.getName().endsWith(".tar.gz")) {
            // Collection of solutions
            final List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(tar);
            System.out.println("Solutions found in tar: " + sols.size());

            final MasonSimState state = sim.getSimState(sols.get(0).getController(), startSeed);

            final SolutionsList frame = new SolutionsList();
            frame.populateTable(sols);
            frame.setHandler(new SolutionsList.SolutionSelectionHandler() {
                @Override
                public void solutionSelected(PersistentSolution sol) {
                    state.setGroupController(sol.getController());
                }
            });

            GUIState gui = sim.getSimStateUI(state);
            Controller contr = gui.createController();
            frame.setVisible(true);
            contr.registerFrame(frame);
        } else {
            // One solution
            GroupController controller = ReevaluationTools.createController(args);
            MasonSimState state = sim.getSimState(controller, startSeed);
            GUIState gui = sim.getSimStateUI(state);
            gui.createController();
        }
    }
}
