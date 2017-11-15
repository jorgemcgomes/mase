/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import mase.mason.world.GeomUtils.Multiline;
import sim.display.GUIState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeSimulator extends MasonSimulationProblem<MazeTask> {

    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        MazeParams par = (MazeParams) params;
        // Do additional setup of parameters (build the maze)
        par.maze = new Multiline[par.mazeFile.length];
        if(par.startPos != null && par.startPos.length != par.maze.length) {
            state.output.fatal("Number of startPos ("+par.startPos.length +") different from number of mazes (" + par.maze.length + ")");
        }
        if(par.startPos == null) {
           par.startPos = new Double2D[par.maze.length];
        }
        if(par.targetPos != null && par.targetPos.length != par.maze.length) {
            state.output.fatal("Number of targetPos ("+par.targetPos.length +") different from number of mazes (" + par.maze.length + ")");
        }        
        if(par.targetPos == null) {
           par.targetPos = new Double2D[par.maze.length];
        }        
        if(par.startOrientation != null && par.startOrientation.length != par.maze.length) {
            state.output.fatal("Number of startOrientation ("+par.startOrientation.length +") different from number of mazes (" + par.maze.length + ")");
        }
        if(par.startOrientation == null) {
           par.startOrientation = new double[par.maze.length];
           Arrays.fill(par.startOrientation, Double.NaN);
        }           
        for (int i = 0; i < par.mazeFile.length; i++) {
            try {
                File svg = new File(par.mazeFile[i]);
                if (!svg.exists()) {
                    svg = new File(this.getClass().getResource(par.mazeFile[i]).toURI());
                }
                state.output.message("Maze: " + svg.getAbsolutePath());
                MazeReader mr = new MazeReader(svg);
                par.maze[i] = new Multiline(mr.getSegments());
                if (par.startPos[i] == null) {
                    par.startPos[i] = mr.getStart();
                }
                if (par.targetPos[i] == null) {
                    par.targetPos[i] = mr.getEnd();
                }
                if (Double.isNaN(par.startOrientation[i])) {
                    Double2D dir = par.targetPos[i].subtract(par.startPos[i]);
                    par.startOrientation[i] = dir.angle();
                }
                state.output.message("Loaded maze " + i + " with " + par.maze[i].segments.length + " segments.\n"
                        + "Bounds: " + par.maze[i].boundingBox + "\n"
                        + par.startPos[i] + " --> " + par.targetPos[i]);
            } catch (Exception ex) {
                state.output.fatal(par.mazeFile[i] + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public GUIState getSimStateUI(MazeTask state) {
        double w = state.par.maze[0].width; // TODO: should be max width/ max height of all mazes
        double h = state.par.maze[0].height;
        double ratio = 1000 / Math.max(w, h);
        return new GUIState2D(state, "Maze", (int) Math.round(w * ratio), (int) Math.round(h * ratio), Color.WHITE);
    }

}
