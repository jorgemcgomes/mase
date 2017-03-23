/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mase.controllers.GroupController;
import mase.mason.world.CircularObject;
import mase.mason.world.PolygonUtils.Segment;
import mase.mason.world.StaticMultilineObject;
import mase.mason.world.StaticPointObject;
import mase.stat.Reevaluate;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Bag;
import sim.util.Int2D;

/**
 *
 * @author jorge
 */
public class MasonTracer {

    public static final String SIZE = "-s";
    public static final String OUT = "-o";
    public static final String SEED = "-seed";
    public static final String BG_COLOR = "-bgcolor";

    public static void main(String[] args) throws Exception {
        int size = 0;
        File out = null;
        int index;
        long seed = new Random().nextLong();
        Color bgColor = null;

        for (index = 0; index < args.length; index++) {
            if (args[index].equals(SIZE)) {
                size = Integer.parseInt(args[1 + index++]);
            } else if (args[index].equals(OUT)) {
                out = new File(args[1 + index++]);
            } else if (args[index].equals(SEED)) {
                seed = Long.parseLong(args[1 + index++]);
            } else if (args[index].equals(BG_COLOR)) {
                bgColor = parseColor(args[1 + index++]);

            }
        }

        File gc = Reevaluate.findControllerFile(args);
        if (out == null && gc != null) {
            out = new File(gc.getParentFile(), gc.getName() + ".svg");
        }

        GroupController controller = Reevaluate.createController(args);
        MasonSimulationProblem sim = (MasonSimulationProblem) Reevaluate.createSimulator(args, gc.getParentFile());
        MasonSimState simState = sim.getSimState(controller, seed);

        MasonTracer tracer = new MasonTracer(size);
        if (bgColor != null) {
            tracer.canvasColor = bgColor;
        }
        tracer.trace(simState, out);
    }

    private final int size;
    public boolean drawInitialPositions = true;
    public boolean drawFinalPositions = true;
    public boolean drawArena = true;
    public boolean collapseRepeated = true;
    public Color canvasColor = Color.WHITE;

    public MasonTracer(int size) {
        this.size = size;
    }

    public void trace(MasonSimState simState, File out) throws IOException {
        simState.start();
        FieldPortrayal2D port = simState.createFieldPortrayal();
        simState.setupPortrayal(port);
        Continuous2D field = (Continuous2D) port.getField();

        double scale = (double) size / Math.max(field.width, field.height);

        int w = (int) (field.width * scale);
        int h = (int) (field.height * scale);

        // create canvas
        SVGGraphics2D gr = new SVGGraphics2D(w, h);
        gr.setPaint(canvasColor);

        // draw area
        if (drawArena) {
            gr.fillRect(0, 0, w, h);
            gr.setPaint(Color.BLACK);
            gr.drawPolygon(new int[]{0, w, w, 0, 0}, new int[]{0, 0, h, h, 0}, 5);
        }

        // draw other objects
        Bag allObjects = field.getAllObjects();
        ArrayList<CircularObject> tracked = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof CircularObject) {
                tracked.add((CircularObject) o);
            } else if (o instanceof StaticPointObject) {
                StaticPointObject spo = (StaticPointObject) o;
                int x = (int) Math.round(spo.getCenterLocation().x * scale);
                int y = (int) Math.round(spo.getCenterLocation().y * scale);
                gr.setPaint(spo.paint);
                gr.drawLine(x, y, x, y);
            } else if (o instanceof StaticMultilineObject) {
                StaticMultilineObject smo = (StaticMultilineObject) o;
                gr.setPaint(smo.paint);
                for (Segment s : smo.segments()) {
                    int x1 = (int) Math.round(s.start.x * scale);
                    int y1 = (int) Math.round(s.start.y * scale);
                    int x2 = (int) Math.round(s.end.x * scale);
                    int y2 = (int) Math.round(s.end.y * scale);
                    gr.drawLine(x1, y1, x2, y2);
                }
            }
        }

        // draw initial positions
        if (drawInitialPositions) {
            for (CircularObject ag : tracked) {
                int x = (int) Math.round((ag.getCenterLocation().x - ag.getRadius() / 2) * scale);
                int y = (int) Math.round((ag.getCenterLocation().y - ag.getRadius() / 2) * scale);
                int s = (int) Math.round(ag.getRadius() * scale);
                gr.setPaint(ag.paint);
                gr.fillRect(x, y, s, s);
            }
        }

        // record paths
        boolean keepGoing = true;
        HashMap<CircularObject, List<Int2D>> points = new HashMap<>();
        while (keepGoing) {
            for (CircularObject ag : tracked) {
                if (!points.containsKey(ag)) { // init
                    points.put(ag, new ArrayList<Int2D>());
                }
                if (field.exists(ag)) {
                    points.get(ag).add(new Int2D((int) Math.round(ag.getCenterLocation().x * scale),
                            (int) Math.round(ag.getCenterLocation().y * scale)));
                }
            }
            keepGoing = simState.schedule.step(simState);
        }
        for (CircularObject ag : tracked) {
            points.get(ag).add(new Int2D((int) Math.round(ag.getCenterLocation().x * scale),
                    (int) Math.round(ag.getCenterLocation().y * scale)));
        }

        // collapse lists
        if (collapseRepeated) {
            for (List<Int2D> pts : points.values()) {
                Int2D last = null;
                Iterator<Int2D> iter = pts.iterator();
                while (iter.hasNext()) {
                    Int2D next = iter.next();
                    if (last != null && next.equals(last)) {
                        iter.remove();
                    }
                    last = next;
                }
            }
        }

        // draw paths
        for (CircularObject ag : points.keySet()) {
            List<Int2D> pts = points.get(ag);
            gr.setPaint(ag.paint);

            int[] xs = new int[pts.size()];
            int[] ys = new int[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                xs[i] = pts.get(i).x;
                ys[i] = pts.get(i).y;
            }
            gr.drawPolyline(xs, ys, pts.size());
        }

        // last positions
        if (drawFinalPositions) {
            for (CircularObject ag : tracked) {
                int x = (int) Math.round((ag.getCenterLocation().x - ag.getRadius()) * scale);
                int y = (int) Math.round((ag.getCenterLocation().y - ag.getRadius()) * scale);
                int s = (int) Math.round(ag.getRadius() * 2 * scale);
                gr.setPaint(ag.paint);
                gr.fillOval(x, y, s, s);
            }
        }

        SVGUtils.writeToSVG(out, gr.getSVGElement());
    }

    public static Color parseColor(String str) {
        String[] split = str.split("-|\\.|,|;");
        if (split.length == 1) {
            try {
                Field field = Color.class.getField(split[0]);
                return (Color) field.get(null);
            } catch (Exception ex) {
                return Color.decode(split[0]);
            }
        } else if (split.length == 3) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } else if (split.length == 4) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }
        return null;
    }

}
