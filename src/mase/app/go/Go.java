/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MaseSimState;
import mase.mason.Mason2dUI;
import mase.mason.SmartAgent;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

/**
 *
 * @author Jorge
 */
public class Go extends MaseSimState {
        
    public static void main(String[] args) {
        Go game = new Go(1, null);
        Mason2dUI ui = new Mason2dUI(game, "Go", 500, 500, Color.RED);
        ui.createController();
    }
    
    protected GoState state;
    protected GoPlayerBoardEvaluation black;
    protected GoPlayerBoardEvaluation white;
    protected GroupController gc;
    private boolean flag; 

    
    public Go(long seed, GroupController gc) {
        super(seed);
        this.gc = gc;
        this.flag = true;
    }

    @Override
    public void start() {
        super.start();
        this.state = new GoState(5);
        AgentController[] controllers = this.gc.getAgentControllers(2);
        if(flag) {
            this.black = new GoPlayerBoardEvaluation(this, controllers[0], GoState.BLACK);
            this.white = new GoPlayerBoardEvaluation(this, controllers[1], GoState.WHITE);
        } else {
            this.black = new GoPlayerBoardEvaluation(this, controllers[1], GoState.BLACK);
            this.white = new GoPlayerBoardEvaluation(this, controllers[0], GoState.WHITE);
        }
        flag = !flag;
        
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
        vgp.setField(state.getGrid());
        SimpleColorMap map = new SimpleColorMap(new Color[]{Color.BLACK, Color.WHITE, Color.GRAY});
        vgp.setMap(map);
    }
    
    
    
}
