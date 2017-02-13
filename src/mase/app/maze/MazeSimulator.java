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
import java.io.File;
import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import mase.mason.world.StaticPolygonObject;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeSimulator extends MasonSimulationProblem<MazeTask> {

    private static final long serialVersionUID = 1L;

    private MazeParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new MazeParams();
        ParamUtils.autoSetParameters(par, state, base, defaultBase(), false);

        try {
            File svg = new File(par.mazeFile);
            if (!svg.exists()) {
                svg = new File(this.getClass().getResource(par.mazeFile).toURI());
            }
            state.output.message("Maze: " + svg.getAbsolutePath());
            MazeReader mr = new MazeReader(svg);
            par.maze = new StaticPolygonObject(mr.getSegments());
            par.maze.filled = false;
            par.maze.paint = Color.BLACK;
            par.maze.setStroke(new BasicStroke(2f));
            par.startPos = mr.getStart();
            par.targetPos = mr.getEnd();
            Double2D dir = par.targetPos.subtract(par.startPos);
            par.startOrientation = dir.angle();
        } catch (Exception ex) {
            state.output.fatal(par.mazeFile + ": " + ex.getMessage());
        }
    }

    @Override
    public MazeTask createSimState(GroupController gc, long seed) {
        return new MazeTask(seed, par, gc);
    }

    /*@Override
    public GUIState getSimStateUI(GroupController gc, long seed) {
        double w = par.maze.getWidth();
        double h = par.maze.getHeight();
        double ratio = 500 / Math.max(w, h);
        return new GUIState2D(createSimState(gc, seed), "Maze",
                (int) Math.round(w * ratio), (int) Math.round(h * ratio), Color.WHITE);
    }*/

}
