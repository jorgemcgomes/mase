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
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evorbc.gp.GPArbitratorController;
import mase.mason.SolutionsList.SolutionSelectionHandler;
import mase.mason.world.CircularObject;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.GeomUtils.Segment;
import mase.mason.world.MultilineObject;
import mase.mason.world.PointObject;
import mase.mason.world.SmartAgent;
import mase.stat.PersistentSolution;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import mase.util.CommandLineUtils;
import org.apache.commons.lang3.StringUtils;
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
    public static final String INDEX = "-index";

    public static void main(String[] args) throws Exception {
        int size = 0;
        File out = null;
        int index;
        long seed = new Random().nextLong();
        Color bgColor = null;
        List<Integer> indexes = new ArrayList<>();

        for (index = 0; index < args.length; index++) {
            if (args[index].equals(SIZE)) {
                size = Integer.parseInt(args[1 + index++]);
            } else if (args[index].equals(OUT)) {
                out = new File(args[1 + index++]);
            } else if (args[index].equals(SEED)) {
                seed = Long.parseLong(args[1 + index++]);
            } else if (args[index].equals(BG_COLOR)) {
                bgColor = ParamUtils.parseColor(args[1 + index++]);
            } else if (args[index].equals(INDEX)) {
                for (int k = index + 1; k < args.length; k++) {
                    if (StringUtils.isNumeric(args[k])) {
                        indexes.add(Integer.valueOf(args[k]));
                        index++;
                    } else {
                        break;
                    }
                }

            }
        }

        File gc = CommandLineUtils.getFileFromArgs(args, ReevaluationTools.P_CONTROLLER, true);
        if (gc == null) {
            System.err.println("Controller(s) not found! Use -gc");
            System.exit(1);
        }

        final MasonTracer tracer = new MasonTracer(size);
        if (bgColor != null) {
            tracer.canvasColor = bgColor;
        }
        final MasonSimulationProblem sim = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, gc.getParentFile());

        if (gc.getName().endsWith(".tar.gz")) {
            // Collection of controllers
            if (out == null) {
                out = gc.getParentFile();
            }
            if (!out.isDirectory()) {
                System.err.println("Provided output is not a directory: " + out.getAbsolutePath());
                System.exit(1);
            }
            final File outDir = out;
            final long fSeed = seed;

            SolutionsList frame = new SolutionsList();
            List<PersistentSolution> sols = SolutionPersistence.readSolutionsFromTar(gc);
            frame.populateTable(sols);
            frame.setHandler(new SolutionSelectionHandler() {
                @Override
                public void solutionSelected(PersistentSolution sol) {
                    File o = new File(outDir, SolutionPersistence.autoFileName(sol) + ".svg");
                    MasonSimState simState = sim.getSimState(sol.getController(), fSeed);
                    try {
                        tracer.trace(simState, o);
                    } catch (Exception ex) {
                        System.err.println("Error tracing " + SolutionPersistence.autoFileName(sol));
                        ex.printStackTrace();
                    }
                }
            });

            frame.setVisible(true);

            if (!indexes.isEmpty()) {
                for (PersistentSolution s : sols) {
                    if (indexes.contains(s.getIndex())) {
                        File o = new File(outDir, SolutionPersistence.autoFileName(s) + ".svg");
                        MasonSimState simState = sim.getSimState(s.getController(), fSeed);
                        try {
                            tracer.trace(simState, o);
                        } catch (Exception ex) {
                            System.err.println("Error tracing " + SolutionPersistence.autoFileName(s));
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            // One controller
            if (out == null) {
                out = new File(gc.getParentFile(), gc.getName() + ".svg");
            }
            GroupController controller = ReevaluationTools.createController(args);
            MasonSimState simState = sim.getSimState(controller, seed);
            tracer.trace(simState, out);
        }

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
        // TODO: assumes that only CircularObjects can move -- not true anymore
        Bag allObjects = field.getAllObjects();
        ArrayList<CircularObject> tracked = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof CircularObject) {
                tracked.add((CircularObject) o);
            } else if (o instanceof PointObject) {
                PointObject spo = (PointObject) o;
                int x = (int) Math.round(spo.getLocation().x * scale);
                int y = (int) Math.round(spo.getLocation().y * scale);
                gr.setPaint(spo.paint);
                gr.drawLine(x, y, x, y);
            } else if (o instanceof MultilineObject) {
                MultilineObject smo = (MultilineObject) o;
                gr.setPaint(smo.paint);
                for (Segment s : smo.getPolygon().segments) {
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
                int x = (int) Math.round((ag.getLocation().x - ag.getRadius() / 2) * scale);
                int y = (int) Math.round((ag.getLocation().y - ag.getRadius() / 2) * scale);
                int s = (int) Math.round(ag.getRadius() * scale);
                gr.setPaint(ag.paint);
                gr.fillRect(x, y, s, s);
            }
        }

        // run simulation and record paths
        boolean keepGoing = true;
        HashMap<CircularObject, List<Int2D>> points = new HashMap<>();
        HashMap<CircularObject, Integer> lastPrimitive = new HashMap<>(); // only used for primitive-based controller logging
        while (keepGoing) {
            for (CircularObject ag : tracked) {
                // Log position
                if (!points.containsKey(ag)) { // init
                    points.put(ag, new ArrayList<Int2D>());
                }
                if (field.exists(ag)) {
                    points.get(ag).add(new Int2D((int) Math.round(ag.getLocation().x * scale),
                            (int) Math.round(ag.getLocation().y * scale)));
                }
                // Log primitive change events
                if(ag instanceof SmartAgent) {
                    AgentController ac = ((SmartAgent) ag).getAgentController();
                    if(ac instanceof GPArbitratorController && ((GPArbitratorController) ac).getLastPrimitive() != null) {
                        int newP = ((GPArbitratorController) ac).getLastPrimitive().id;
                        if(lastPrimitive.containsKey(ag) && newP != lastPrimitive.get(ag)) {
                            int x = (int) Math.round((ag.getLocation().x - ag.getRadius() / 4) * scale);
                            int y = (int) Math.round((ag.getLocation().y - ag.getRadius() / 4) * scale);
                            int s = (int) Math.round(ag.getRadius() / 2 * scale);
                            gr.setPaint(ag.paint);
                            gr.drawOval(x, y, s, s);
                        }
                        lastPrimitive.put(ag, newP);
                    }
                }
            }
            keepGoing = simState.schedule.step(simState);
        }
        for (CircularObject ag : tracked) {
            points.get(ag).add(new Int2D((int) Math.round(ag.getLocation().x * scale),
                    (int) Math.round(ag.getLocation().y * scale)));
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
                int x = (int) Math.round((ag.getLocation().x - ag.getRadius()) * scale);
                int y = (int) Math.round((ag.getLocation().y - ag.getRadius()) * scale);
                int s = (int) Math.round(ag.getRadius() * 2 * scale);
                gr.setPaint(ag.paint);
                gr.fillOval(x, y, s, s);

                // draw orientation
                if (ag instanceof EmboddiedAgent) {
                    EmboddiedAgent ea = (EmboddiedAgent) ag;
                    double ori = ea.orientation2D();
                    gr.setPaint(ea.paint.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
                    int cx = (int) Math.round(ag.getLocation().x * scale);
                    int cy = (int) Math.round(ag.getLocation().y * scale);
                    int ox = (int) Math.round(Math.cos(ori) * ag.getRadius() * scale);
                    int oy = (int) Math.round(Math.sin(ori) * ag.getRadius() * scale);
                    gr.drawLine(cx, cy, cx + ox, cy + oy);
                }
            }
        }

        SVGUtils.writeToSVG(out, gr.getSVGElement());
    }

}
