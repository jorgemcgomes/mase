/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.world;

import java.io.File;
import mase.mason.world.StaticPolygon.Segment;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PolygonGenerator {
     
    /**
     * Format by segment: x1,y1,x2,y2;x3,y3,x4,y4;...
     * @param s
     * @return 
     */
    public static StaticPolygon generateFromSegments(String s) {
        String[] segStrings = s.split(";");
        Segment[] segments = new Segment[segStrings.length];
        for(int i = 0 ; i < segStrings.length ; i++) {
            String[] v = segStrings[i].split(",");
            Double2D start = new Double2D(Double.parseDouble(v[0].trim()), Double.parseDouble(v[1].trim())); 
            Double2D end = new Double2D(Double.parseDouble(v[2].trim()), Double.parseDouble(v[3].trim())); 
            segments[i] = new Segment(start,end);
        }
        StaticPolygon pol = new StaticPolygon(segments);
        return pol;
    }
    
    /**
     * Format by point: x1,y1;x2,y2;x3,y3;...
     * @param s
     * @return 
     */
    public static StaticPolygon generateFromPoints(String s) {
        String[] pointStrings = s.split(";");
        Double2D[] points = new Double2D[pointStrings.length];
        for(int i = 0 ; i < pointStrings.length ; i++) {
            String[] v = pointStrings[i].split(",");
            points[i] = new Double2D(Double.parseDouble(v[0].trim()), Double.parseDouble(v[1].trim()));
        }
        StaticPolygon pol = new StaticPolygon(points);
        return pol;
    }
    
}
