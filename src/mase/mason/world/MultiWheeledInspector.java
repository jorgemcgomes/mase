/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.SwingUtilities;
import org.apache.commons.math3.util.FastMath;
import sim.portrayal.Inspector;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * TODO: this should be refractored to rely on Inspector's
 * @author jorge
 */
public class MultiWheeledInspector extends Inspector {

    public static final double F = 3; // Factor that multiplies the length of the speed vectors to be more visible
    private static final long serialVersionUID = 1L;
    private final MultipleWheelAxesActuator act;
    private final double scale ;
    private final Int2D center;
    private final int drawSize;
    
    public MultiWheeledInspector(MultipleWheelAxesActuator act, int graphicsSize) {
        setBackground(Color.WHITE);
        this.drawSize = graphicsSize;
        this.center = new Int2D(drawSize / 2, drawSize / 2);
        this.act = act;
        scale = drawSize / (act.ag.getRadius() * 4); // the robot should occupy half the width of the panel
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        int size = (int) (act.ag.getRadius() * 2 * scale);
        g2.drawRect(center.x - size / 2, center.y - size / 2, size, size);
        double[] r = act.getCurrentRotations();
        for (int i = 0; i < act.wheelPos.length; i++) {
            int d = (int) (act.maxSpeed * 2 * F * scale);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.GRAY);
            g2.drawOval(tx(act.wheelPos[i].x) - d / 2, ty(act.wheelPos[i].y) - d / 2, d, d);
            drawLine(g2, act.wheelPos[i], act.maxSpeed * F, act.rotationLimit);
            drawLine(g2, act.wheelPos[i], act.maxSpeed * F, -act.rotationLimit);
            drawLine(g2, act.wheelPos[i], -act.maxSpeed * F, act.rotationLimit);
            drawLine(g2, act.wheelPos[i], -act.maxSpeed * F, -act.rotationLimit);
            g2.drawString(i + "", tx(act.wheelPos[i].x), ty(act.wheelPos[i].y));
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2));
            drawLine(g2, act.wheelPos[i], act.maxSpeed * F, r[i]);
        }
        
        // draw speed
        double s = act.getCurrentSpeed();
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.GRAY);
        int SW = 20;
        g2.drawRect(drawSize, drawSize / 4, SW, drawSize / 2); // box
        g2.drawLine(drawSize, drawSize / 2, drawSize + SW, drawSize / 2); // half line
        g2.setColor(Color.RED);
        if(s > 0) { // Positive speed, paint upwards
            int f = (int) ((s / act.maxSpeed) * (drawSize / 4));
            g2.fillRect(drawSize, drawSize / 2 - f, SW, f);
        } else if(s < 0) { // Positive speed, paint downwards
            g2.fillRect(drawSize, drawSize / 2 + 1, SW, drawSize / 2 + (int) ((Math.abs(s) / act.maxSpeed) * (drawSize / 4)));
        }
        
    }

    private int tx(double x) {
        return (int) Math.round(center.x + x * scale);
    }

    private int ty(double y) {
        return (int) Math.round(center.y + y * scale);
    }

    private void drawLine(Graphics2D g, Double2D origin, double length, double angle) {
        Double2D dest = origin.add(new Double2D(FastMath.cos(angle) * length, FastMath.sin(angle) * length));
        g.drawLine(tx(origin.x), ty(origin.y), tx(dest.x), ty(dest.y));
    }

    @Override
    public void updateInspector() {
        SwingUtilities.invokeLater(new Runnable() { public void run() { repaint(); }});
    }
}
