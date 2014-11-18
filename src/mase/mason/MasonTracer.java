/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import mase.controllers.GroupController;
import static mase.mason.MasonPlayer.P_AGENT_CONTROLLER;
import static mase.mason.MasonPlayer.P_CONTROLLER;
import static mase.mason.MasonPlayer.createController;
import static mase.mason.MasonPlayer.createSimulator;
import mase.mason.world.EmboddiedAgent;
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
            } else if(args[index].equals(SEED)) {
                seed = Long.parseLong(args[1 + index++]);
            }
        }

        GroupController controller = createController(args);
        MasonSimulator sim = createSimulator(args);
        trace(controller, sim, seed, size, out);

    }

    public static void trace(GroupController gc, MasonSimulator sim, long seed, int size, File out) throws IOException {
        GUICompatibleSimState simState = sim.createSimState(gc, seed);

        simState.start();
        FieldPortrayal2D port = simState.createFieldPortrayal();
        simState.setupPortrayal(port);
        Continuous2D field = (Continuous2D) port.getField();

        double scale = (double) size / Math.max(field.width, field.height);

        Bag allObjects = field.getAllObjects();
        ArrayList<EmboddiedAgent> agents = new ArrayList<EmboddiedAgent>();
        for (Object o : allObjects) {
            if (o instanceof EmboddiedAgent) {
                agents.add((EmboddiedAgent) o);
            }
        }
        ArrayList<Int2D> lastPoint = new ArrayList<Int2D>(agents.size());
        BufferedImage img = new BufferedImage((int) (field.width * scale),
                (int) (field.height * scale), BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = img.createGraphics();
        gr.setPaint(Color.WHITE);
        gr.fillRect(0, 0, size, size);
        gr.setPaint(Color.BLACK);
        gr.drawRect(0, 0, size - 1, size - 1);

        // draw initial positions
        for (EmboddiedAgent ag : agents) {
            int x = (int) Math.round((ag.getLocation().x - ag.getRadius()/2) * scale);
            int y = (int) Math.round((ag.getLocation().y - ag.getRadius()/2) * scale);
            int s = (int) Math.round(ag.getRadius() * scale);
            Color c = (Color) ag.paint;
            gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
            gr.fillOval(x, y, s, s);
            lastPoint.add(new Int2D((int) Math.round(ag.getLocation().x * scale),
                    (int) Math.round(ag.getLocation().y * scale)));
        }

        // draw paths
        boolean keepGoing = true;
        while (keepGoing && simState.schedule.getSteps() < sim.maxSteps) {
            keepGoing = simState.schedule.step(simState);
            for (int a = 0; a < agents.size(); a++) {
                EmboddiedAgent ag = agents.get(a);
                int x = (int) Math.round(ag.getLocation().x * scale);
                int y = (int) Math.round(ag.getLocation().y * scale);

                Int2D old = lastPoint.get(a);
                Color c = (Color) ag.paint;
                gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
                gr.drawLine(old.x, old.y, x, y);
                lastPoint.set(a, new Int2D(x, y));
            }
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

        ImageIO.write(img, "png", out);
    }

}
