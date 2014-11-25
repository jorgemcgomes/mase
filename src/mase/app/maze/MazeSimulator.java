/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.maze;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.BasicStroke;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUICompatibleSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulator;
import mase.mason.world.PolygonGenerator;
import mase.mason.world.StaticPolygon;
import sim.display.GUIState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeSimulator extends MasonSimulator {
    
    private MazeParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new MazeParams();
        Parameter df = defaultBase();
        par.linearSpeed = state.parameters.getDouble(df.push(MazeParams.P_LINEAR_SPEED), null);
        par.turnSpeed = state.parameters.getDouble(df.push(MazeParams.P_TURN_SPEED), null);
        par.agentRadius = state.parameters.getDouble(df.push(MazeParams.P_AGENT_RADIUS), null);
        String t = state.parameters.getString(df.push(MazeParams.P_MAZE), null);
        StaticPolygon pol = PolygonGenerator.generateFromSegments(t);
        pol.filled = false;
        pol.paint = Color.BLACK;
        pol.setStroke(new BasicStroke(2f));
        par.maze = pol;
        t = state.parameters.getString(df.push(MazeParams.P_START_POS), null);
        String[] s = t.split(",");
        par.startPos = new Double2D(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        t = state.parameters.getString(df.push(MazeParams.P_TARGET_POS), null);
        s = t.split(",");
        par.targetPos = new Double2D(Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        par.targetRadius = state.parameters.getDouble(df.push(MazeParams.P_TARGET_RADIUS), null);
        par.startOrientation = state.parameters.getDouble(df.push(MazeParams.P_START_ORI), null);
        par.sensorRange = state.parameters.getDouble(df.push(MazeParams.P_SENSOR_RANGE), null);
    }
    
    

    @Override
    public GUICompatibleSimState createSimState(GroupController gc, long seed) {
        return new MazeTask(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        double w = par.maze.getWidth();
        double h = par.maze.getHeight();
        double ratio = 500 / Math.max(w, h);
        return new GUIState2D(createSimState(gc, seed), "Maze", 
                (int) Math.round(w * ratio), (int) Math.round(h * ratio), Color.WHITE);
    }
    
}
