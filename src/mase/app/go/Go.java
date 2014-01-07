/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MaseSimState;
import mase.mason.SmartAgent;
import sim.field.grid.IntGrid2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

/**
 *
 * @author Jorge
 */
public class Go extends MaseSimState {

    /*public static void main(String[] args) {
     Go game = new Go(1, null);
     Mason2dUI ui = new Mason2dUI(game, "Go", 500, 500, Color.RED);
     ui.createController();
     }*/
    protected GoState state;
    protected IntGrid2D grid;
    protected GoPlayer black;
    protected GoPlayer white;
    protected GroupController gc;
    protected LinkedList<GoState> history;
    protected ControllerMode mode;
    protected int boardSize;

    public enum ControllerMode {

        board, position
    }

    /*
    BLACK = controller[0] ; WHITE = controller[1]
    */
    public Go(long seed, GroupController gc, ControllerMode mode, int boardSize) {
        super(seed);
        this.gc = gc;
        this.mode = mode;
        this.boardSize = boardSize;
    }

    @Override
    public void start() {
        super.start();
        this.state = new GoState(boardSize);
        this.history = new LinkedList<GoState>();
        AgentController[] controllers = this.gc.getAgentControllers(2);
        if (mode == ControllerMode.board) {
            this.black = new GoPlayerBoardEvaluation(this, controllers[GoState.BLACK], GoState.BLACK);
            this.white = new GoPlayerBoardEvaluation(this, controllers[GoState.WHITE], GoState.WHITE);
        } else if (mode == ControllerMode.position) {
            this.black = new GoPlayerMoveEvaluation(this, controllers[GoState.BLACK], GoState.BLACK);
            this.white = new GoPlayerMoveEvaluation(this, controllers[GoState.WHITE], GoState.WHITE);
        }

        this.schedule.scheduleRepeating(0.0, black, 2.0);
        this.schedule.scheduleRepeating(1.0, white, 2.0);
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public FieldPortrayal2D createFieldPortrayal() {
        ValueGridPortrayal2D port = new ValueGridPortrayal2D();
        return port;
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        ValueGridPortrayal2D vgp = (ValueGridPortrayal2D) port;
        this.grid = new IntGrid2D(boardSize, boardSize, GoState.EMPTY);
        vgp.setField(grid);
        SimpleColorMap map = new SimpleColorMap(new Color[]{Color.BLACK, Color.WHITE, Color.GRAY});
        vgp.setMap(map);
    }

}
