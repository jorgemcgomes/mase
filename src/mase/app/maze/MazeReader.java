/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.maze;

import com.kitfox.svg.Path;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import mase.mason.world.StaticPolygon.Segment;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeReader {
    
    public static final String MAZE_ID = "maze";
    public static final String START_ID = "start";
    public static final String END_ID = "end";
    private final SVGDiagram diagram;
    
    public MazeReader(File f) throws FileNotFoundException {
        SVGUniverse univ = new SVGUniverse();
        URI uri = univ.loadSVG(new FileReader(f), "maze");
        diagram = univ.getDiagram(uri);
    }
    
    public Segment[] getSegments() throws SVGException {
        SVGElement maze = diagram.getElement(MAZE_ID);
        List<Segment> segs = new ArrayList<Segment>();
        for(Object o : maze.getChildren(null)) {
            if(o instanceof ShapeElement) {
                Shape shape = ((ShapeElement) o).getShape();
                PathIterator iter = shape.getPathIterator(null,0.001d);
                Double2D last = null;
                while(!iter.isDone()) {
                    double[] coords = new double[2];
                    iter.currentSegment(coords);
                    Double2D curr = new Double2D(coords[0],coords[1]);
                    if(last != null) {
                        segs.add(new Segment(last, curr));
                    }
                    last = curr;
                    iter.next();
                }
            }
        }
        return segs.toArray(new Segment[segs.size()]);
    }
    
    public Double2D getStart() throws SVGException {
        return getPoint(START_ID);
    }
    
    public Double2D getEnd() throws SVGException {
        return getPoint(END_ID);
    }
    
    private Double2D getPoint(String id) throws SVGException {
        SVGElement point = diagram.getElement(id);
        ShapeElement shape = (ShapeElement) point;
        Rectangle2D bb = shape.getBoundingBox();
        return new Double2D(bb.getX(),bb.getY());
    }
    
}
