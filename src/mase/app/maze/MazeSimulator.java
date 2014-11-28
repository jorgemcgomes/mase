/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import com.kitfox.svg.SVGException;
import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        par.sensorRange = state.parameters.getDouble(df.push(MazeParams.P_SENSOR_RANGE), null);
        par.targetRadius = state.parameters.getDouble(df.push(MazeParams.P_TARGET_RADIUS), null);

        try {
            String svgPath = state.parameters.getString(df.push(MazeParams.P_MAZE), null);
            File svg = new File(svgPath);
            if (!svg.exists()) {
                svg = new File(this.getClass().getResource(svgPath).toURI());
            }
            state.output.message("Maze: " + svg.getAbsolutePath());
            MazeReader mr = new MazeReader(svg);
            par.maze = new StaticPolygon(mr.getSegments());
            par.maze.filled = false;
            par.maze.paint = Color.BLACK;
            par.maze.setStroke(new BasicStroke(2f));
            par.startPos = mr.getStart();
            par.targetPos = mr.getEnd();
            Double2D dir = par.targetPos.subtract(par.startPos);
            par.startOrientation = dir.angle();
        } catch (Exception ex) {
            state.output.fatal(ex.getMessage(), df.push(MazeParams.P_MAZE));
        }
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
