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
    protected double[] min;
    protected double[] max;
    protected double[] range;
    //private final Int2D center;
    protected final int drawSize;
    protected final int pSize;
    protected static final int MARGIN = 10;

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

    public void setRepertoireBounds(double[] min, double[] max) {
        this.range = new double[min.length];
        for(int i = 0 ; i < range.length ; i++) {
            range[i] = max[i] - min[i];
        }
        this.min = min;
        this.max = max;
    }

    public void setBounds(Collection<Primitive> primitives) {
        double[] min = new double[primitives.iterator().next().coordinates.length];
        double[] max = new double[min.length];
        for (Primitive p : primitives) {
            for(int i = 0 ; i < p.coordinates.length ; i++) {
                min[i] = Math.min(min[i], p.coordinates[i]);
                max[i] = Math.max(max[i], p.coordinates[i]);
            }
        }
        this.setRepertoireBounds(min, max);
    }

    protected void drawAxis(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        if(range.length == 2) {
            g.drawRect(tx(min[0]), ty(min[1]), tx(max[0]) - tx(min[0]), ty(max[1]) - ty(min[1]));
            g.drawLine(tx(0), ty(min[1]), tx(0), ty(max[1]));
            g.drawLine(tx(min[0]), ty(0), tx(max[0]), ty(0));
        } else {
            for(int i = 0 ; i < range.length ; i++) {
                g.drawLine(pca(i), pcv(min[i], i), pca(i), pcv(max[i], i));
            }
            // TODO: missing ticks
        }
        
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
        drawPrimitive(p.coordinates, c, g);
    }

    protected void drawPrimitive(double[] coordinates, Color c, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(c);
        if(range.length == 2) {
            g2.fillOval(tx(coordinates[0]) - pSize / 2, ty(coordinates[1]) - pSize / 2, pSize, pSize);
        } else {
            for(int i = 1 ; i < range.length ; i++) {
                g2.drawLine(pca(i-1), pcv(coordinates[i-1], i-1), pca(i), pcv(coordinates[i], i));
            }
        }
    }
    
    

    protected void highlightPrimitive(Primitive p, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.RED);
        if(range.length == 2) {
            g2.drawOval(tx(p.coordinates[0]) - pSize / 2 - 3, ty(p.coordinates[1]) - pSize / 2 - 3, pSize + 7, pSize + 7);
        } else {
            drawPrimitive(p, Color.RED, g);
            for(int i = 0 ; i < range.length ; i++) {
                g2.fillOval(pca(i) - pSize / 2, pcv(p.coordinates[i], i) - pSize / 2, pSize, pSize);
            }
        }
        g2.setStroke(new BasicStroke(1));
    }
    
    protected void markSpot(double[] coords, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2));
        if(range.length == 2) {
            // Draw cross
            int tx = tx(coords[0]);
            int ty = ty(coords[1]);
            g2.drawLine(tx - pSize, ty - pSize, tx + pSize, ty + pSize);
            g2.drawLine(tx - pSize, ty + pSize, tx + pSize, ty - pSize);  
        } else {
            drawPrimitive(coords, Color.BLUE, g);
            for(int i = 0 ; i < range.length ; i++) {
                g2.drawOval(pca(i) - pSize / 2, pcv(coords[i], i) - pSize / 2, pSize, pSize);
            }
        }
        g2.setStroke(new BasicStroke(1));
    }
    
    protected int pca(int index) {
        return MARGIN + (int) (drawSize * (index / (double) (range.length - 1)));
    }
    
    protected int pcv(double v, int index) {
        return MARGIN + (int) Math.round(((v - min[index]) / range[index]) * drawSize);
    }

    protected int tx(double x) {
        return MARGIN + (int) Math.round(((x - min[0]) / range[0]) * drawSize); // [0,drawSize]
    }

    protected int ty(double y) {
        return MARGIN + (int) Math.round(((y - min[1]) / range[1]) * drawSize); // [0,drawSize]
    }
}
