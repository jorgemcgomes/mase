/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;
import javax.swing.SwingUtilities;
import mase.evorbc.Repertoire.Primitive;
import sim.portrayal.Inspector;

/**
 *
 * @author jorge
 */
public abstract class RepertoireInspector extends Inspector {

    private static final long serialVersionUID = 1L;
    private double minX, minY, maxX, maxY, range;
    //private final Int2D center;
    private final int drawSize;
    private final int pSize;
    private static final int MARGIN = 10;

    public RepertoireInspector(int canvasSize, int primitiveSize) {
        this.drawSize = canvasSize;
        this.pSize = primitiveSize;
        //this.center = new Int2D(drawSize / 2, drawSize / 2);
        setBackground(Color.WHITE);
    }

    @Override
    public void updateInspector() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });
    }

    public void setRepertoireBounds(double minX, double maxX, double minY, double maxY) {
        this.range = Math.max(maxX - minX, maxY - minY);
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setBounds(Collection<Primitive> primitives) {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Primitive p : primitives) {
            minX = Math.min(minX, p.coordinates[0]);
            maxX = Math.max(maxX, p.coordinates[0]);
            minY = Math.min(minY, p.coordinates[1]);
            maxY = Math.max(maxY, p.coordinates[1]);
        }
        this.setRepertoireBounds(minX, maxX, minY, maxY);
    }

    protected void drawAxis(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(tx(minX), ty(minY), tx(maxX) - tx(minX), ty(maxY) - ty(minY));
        g.drawLine(tx(0), ty(minY), tx(0), ty(maxY));
        g.drawLine(tx(minX), ty(0), tx(maxX), ty(0));
    }

    protected void drawPrimitives(Collection<Primitive> primitives, Graphics g) {
        for (Primitive p : primitives) {
            drawPrimitive(p, g);
        }
    }

    protected void drawPrimitives(Collection<Primitive> primitives, Color c, Graphics g) {
        for (Primitive p : primitives) {
            drawPrimitive(p, c, g);
        }
    }

    protected void drawPrimitive(Primitive p, Graphics g) {
        drawPrimitive(p, Color.BLACK, g);
    }

    protected void drawPrimitive(Primitive p, Color c, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(c);
        g2.fillOval(tx(p.coordinates[0]) - pSize / 2, ty(p.coordinates[1]) - pSize / 2, pSize, pSize);
    }

    protected void highlightPrimitive(Primitive p, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.RED);
        g2.drawOval(tx(p.coordinates[0]) - pSize / 2 - 3, ty(p.coordinates[1]) - pSize / 2 - 3, pSize + 7, pSize + 7);
    }

    protected void markSpot(double x, double y, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2));
        int tx = tx(x);
        int ty = ty(y);
        g2.drawLine(tx - pSize, ty - pSize, tx + pSize, ty + pSize);
        g2.drawLine(tx - pSize, ty + pSize, tx + pSize, ty - pSize);
    }

    protected int tx(double x) {
        return MARGIN + (int) Math.round(((x - minX) / range) * drawSize); // [0,drawSize]
    }

    protected int ty(double y) {
        return MARGIN + (int) Math.round(((y - minY) / range) * drawSize); // [0,drawSize]
    }
}
