/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import mase.controllers.GroupController;
import mase.mason.world.EmboddiedAgent;
import mase.stat.Reevaluate;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 *
 * @author jorge
 */
public class MasonTracer {

    public static final String SIZE = "-s";
    public static final String OUT = "-o";
    public static final String SEED = "-seed";
    //public static final String DRAW_AREA = "-draw-area";

    public static void main(String[] args) throws Exception {
        int size = 0;
        File out = null;
        int index;
        long seed = new Random().nextLong();
        for (index = 0; index < args.length; index++) {
            if (args[index].equals(SIZE)) {
                size = Integer.parseInt(args[1 + index++]);
            } else if (args[index].equals(OUT)) {
                out = new File(args[1 + index++]);
            } else if (args[index].equals(SEED)) {
                seed = Long.parseLong(args[1 + index++]);
            }
        }
        File gc = Reevaluate.findControllerFile(args);
        if(out == null && gc != null) {
            out = new File(gc.getParentFile(), gc.getName() + ".svg");
        }

        GroupController controller = Reevaluate.createController(args);
        MasonSimulationProblem sim = (MasonSimulationProblem) Reevaluate.createSimulator(args, gc.getParentFile());
        trace(controller, sim, seed, size, out);

    }

    public static void trace(GroupController gc, MasonSimulationProblem sim, long seed, int size, File out) throws IOException {
        MasonSimState simState = sim.createSimState(gc, seed);

        simState.start();
        FieldPortrayal2D port = simState.createFieldPortrayal();
        simState.setupPortrayal(port);
        Continuous2D field = (Continuous2D) port.getField();

        double scale = (double) size / Math.max(field.width, field.height);

        int w = (int) (field.width * scale);
        int h = (int) (field.height * scale);
        
        // create canvas
        SVGGraphics2D gr = new SVGGraphics2D(w,h);
        gr.setPaint(Color.WHITE);
        
        // draw area
        gr.fillRect(0, 0, w, h);
        gr.setPaint(Color.BLACK);
        gr.drawPolygon(new int[]{0,w,w,0,0}, new int[]{0,0,h,h,0}, 5);        
        
        Bag allObjects = field.getAllObjects();
        ArrayList<EmboddiedAgent> agents = new ArrayList<EmboddiedAgent>();
        for (Object o : allObjects) {
            if (o instanceof EmboddiedAgent) {
                agents.add((EmboddiedAgent) o);
            } else if(o instanceof OvalPortrayal2D) {
                OvalPortrayal2D oval = (OvalPortrayal2D) o;
                Double2D loc = field.getObjectLocation(oval);
                int x = (int) Math.round((loc.x - oval.scale / 2) * scale);
                int y = (int) Math.round((loc.y - oval.scale / 2) * scale);
                int s = (int) Math.round(oval.scale * scale);
                Color c = (Color) oval.paint;
                gr.setPaint(c);
                gr.fillOval(x, y, s, s);         
            }
        }
        
        // draw initial positions
        for (EmboddiedAgent ag : agents) {
            int x = (int) Math.round((ag.getLocation().x - ag.getRadius() / 2) * scale);
            int y = (int) Math.round((ag.getLocation().y - ag.getRadius() / 2) * scale);
            int s = (int) Math.round(ag.getRadius() * scale);
            Color c = (Color) ag.paint;
            gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
            gr.fillRect(x, y, s, s);
        }

        // record paths
        boolean keepGoing = true;
        HashMap<EmboddiedAgent, List<Int2D>> points = new HashMap<EmboddiedAgent, List<Int2D>>();
        while (keepGoing && simState.schedule.getSteps() < sim.maxSteps) {
            for (EmboddiedAgent ag : agents) {
                if (!points.containsKey(ag)) {
                    points.put(ag, new ArrayList<Int2D>());
                }
                points.get(ag).add(new Int2D((int) Math.round(ag.getLocation().x * scale), 
                        (int) Math.round(ag.getLocation().y * scale)));
            }
            keepGoing = simState.schedule.step(simState);
        }
        for (EmboddiedAgent ag : agents) {
            points.get(ag).add(new Int2D((int) Math.round(ag.getLocation().x * scale), 
                    (int) Math.round(ag.getLocation().y * scale)));
        }

        // draw paths
        for (EmboddiedAgent ag : points.keySet()) {
            List<Int2D> pts = points.get(ag);
            Color c = (Color) ag.paint;
            gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));

            int[] xs = new int[pts.size()];
            int[] ys = new int[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                xs[i] = pts.get(i).x;
                ys[i] = pts.get(i).y;
            }
            gr.drawPolyline(xs, ys, pts.size());
        }

        // last positions
        for (EmboddiedAgent ag : agents) {
            int x = (int) Math.round((ag.getLocation().x - ag.getRadius()) * scale);
            int y = (int) Math.round((ag.getLocation().y - ag.getRadius()) * scale);
            int s = (int) Math.round(ag.getRadius() * 2 * scale);
            Color c = (Color) ag.paint;
            gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
            gr.fillOval(x, y, s, s);
        }

        SVGUtils.writeToSVG(out, gr.getSVGElement());
    }

}
