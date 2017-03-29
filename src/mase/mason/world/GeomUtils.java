/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.LinkedHashSet;
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class GeomUtils {

    public static Pair<Double2D, Double2D> computeBB(Double2D[] points) {
        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        for (Double2D p : points) {
            xMin = Math.min(xMin, p.x);
            yMin = Math.min(yMin, p.y);
            xMax = Math.max(xMax, p.x);
            yMax = Math.max(yMax, p.y);
        }
        return Pair.of(new Double2D(xMin, yMin), new Double2D(xMax, yMax));
    }

    // Is p left of the line vw?
    public static boolean isLeftOf(Double2D p, Double2D v, Double2D w) {
        return (p.y - v.y) * (w.x - v.x) > (p.x - v.x) * (w.y - v.y);
    }

    // Return minimum distance between line segment vw and point p
    public static double distToSegment(Double2D p, Double2D v, Double2D w) {
        double l2 = FastMath.pow2(v.x - w.x) + FastMath.pow2(v.y - w.y);
        if (l2 == 0.0) {
            return p.distance(v); // v == w case
        }
        // Consider the line extending the segment, parameterized as v + t (w - v).
        // We find projection of point p onto the line.
        // It falls where t = [(p-v) . (w-v)] / |w-v|^2
        double t = p.subtract(v).dot(w.subtract(v)) / l2;
        if (t < 0.0) {
            return p.distance(v); // Beyond the 'v' end of the segment
        } else if (t > 1.0) {
            return p.distance(w); // Beyond the 'w' end of the segment
        }
        Double2D projection = v.add((w.subtract(v)).multiply(t)); // Projection falls on the segment
        return p.distance(projection);
    }

    // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    public static Double2D segmentIntersection(Double2D p0, Double2D p1, Double2D p2, Double2D p3) {
        double s1x = p1.x - p0.x;
        double s1y = p1.y - p0.y;
        double s2x = p3.x - p2.x;
        double s2y = p3.y - p2.y;
        double s = (-s1y * (p0.x - p2.x) + s1x * (p0.y - p2.y)) / (-s2x * s1y + s1x * s2y);
        double t = (s2x * (p0.y - p2.y) - s2y * (p0.x - p2.x)) / (-s2x * s1y + s1x * s2y);
        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // collision detected
            return new Double2D(p0.x + t * s1x, p0.y + t * s1y);
        } else {
            // no collision
            return null;
        }
    }

    // Return the point belonging to line segment vw that is closest to point p
    public static Double2D closestPointInSegment(Double2D p, Double2D v, Double2D w) {
        double l2 = FastMath.pow2(v.x - w.x) + FastMath.pow2(v.y - w.y);
        if (l2 == 0.0) {
            return v; // v == w case
        }
        // Consider the line extending the segment, parameterized as v + t (w - v).
        // We find projection of point p onto the line.
        // It falls where t = [(p-v) . (w-v)] / |w-v|^2
        double t = p.subtract(v).dot(w.subtract(v)) / l2;
        if (t < 0.0) {
            return v; // Beyond the 'v' end of the segment
        } else if (t > 1.0) {
            return w; // Beyond the 'w' end of the segment
        }
        Double2D projection = v.add((w.subtract(v)).multiply(t)); // Projection falls on the segment
        return projection;
    }

    /*
    only works if closed polygon
     */
    public static boolean pointInPolygon(Double2D test, Double2D[] points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y)
                    && (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }    
    
    /**
     * Format by point: x1,y1;x2,y2;x3,y3;...
     *
     * @param s
     * @return
     */
    public static Multiline generateFromPoints(String s) {
        String[] pointStrings = s.split(";");
        Double2D[] points = new Double2D[pointStrings.length];
        for (int i = 0; i < pointStrings.length; i++) {
            String[] v = pointStrings[i].split(",");
            points[i] = new Double2D(Double.parseDouble(v[0].trim()), Double.parseDouble(v[1].trim()));
        }
        return new Multiline(points);
    }

    /**
     * Format by segment: x1,y1,x2,y2;x3,y3,x4,y4;...
     *
     * @param s
     * @return
     */
    public static Multiline generateFromSegments(String s) {
        String[] segStrings = s.split(";");
        Segment[] segments = new Segment[segStrings.length];
        for (int i = 0; i < segStrings.length; i++) {
            String[] v = segStrings[i].split(",");
            Double2D start = new Double2D(Double.parseDouble(v[0].trim()), Double.parseDouble(v[1].trim()));
            Double2D end = new Double2D(Double.parseDouble(v[2].trim()), Double.parseDouble(v[3].trim()));
            segments[i] = new Segment(start, end);
        }
        return new Multiline(segments);
    }    
    


    public static class Multiline {

        public final Segment[] segments;
        public final Double2D[] points;
        public final Pair<Double2D, Double2D> boundingBox;
        public final Double2D center;
        public final double width, height;

        private Multiline(Segment[] segments, Double2D[] points, Pair<Double2D, Double2D> boundingBox, Double2D center, double width, double height) {
            this.segments = segments;
            this.points = points;
            this.boundingBox = boundingBox;
            this.center = center;
            this.width = width;
            this.height = height;
        }

        public Multiline(Double2D... points) {
            if(points.length < 2) {
                throw new RuntimeException("Not enough points. Need at least 2.");
            }
            this.segments = new Segment[points.length - 1];
            for (int i = 0; i < points.length - 1; i++) {
                segments[i] = new Segment(points[i], points[i + 1]);
            }
            this.points = points;
            this.boundingBox = GeomUtils.computeBB(points);
            this.width = boundingBox.getRight().x - boundingBox.getLeft().x;
            this.height = boundingBox.getRight().y - boundingBox.getLeft().y;
            this.center = new Double2D((boundingBox.getLeft().x + boundingBox.getRight().x) / 2,
                    (boundingBox.getLeft().y + boundingBox.getRight().y) / 2);
        }

        /**
         * Builds a polygon with the given segments
         *
         * @param segs Coordinates are relative to the object location in the
         * field
         */
        public Multiline(Segment... segs) {
            // cleanup line sequences, remove duplicates
            LinkedHashSet<Double2D> ps = new LinkedHashSet<>();
            for (int i = 0; i < segs.length; i++) {
                ps.add(segs[i].start);
                ps.add(segs[i].end);
            }
            this.segments = segs;
            this.points = new Double2D[ps.size()];
            ps.toArray(points);
            this.boundingBox = GeomUtils.computeBB(points);
            this.width = boundingBox.getRight().x - boundingBox.getLeft().x;
            this.height = boundingBox.getRight().y - boundingBox.getLeft().y;
            this.center = new Double2D((boundingBox.getLeft().x + boundingBox.getRight().x) / 2,
                    (boundingBox.getLeft().y + boundingBox.getRight().y) / 2);
        }
        

        public double closestDistance(Double2D rayStart, Double2D rayEnd) {
            double closestDist = Double.POSITIVE_INFINITY;
            for (Segment seg : segments) {
                Double2D inters = GeomUtils.segmentIntersection(rayStart, rayEnd, seg.start, seg.end);
                if (inters != null) {
                    double d = rayStart.distance(inters);
                    if (d < closestDist) {
                        closestDist = d;
                    }
                }
            }
            return closestDist;
        }

        public double closestDistance(Double2D testPoint) {
            double min = Double.POSITIVE_INFINITY;
            for (Segment seg : segments) {
                double d = GeomUtils.distToSegment(testPoint, seg.start, seg.end);
                min = Math.min(min, Math.max(0, d));
            }
            return min;
        }

        public double closestDistance(Multiline other) {
            double closest = Double.NEGATIVE_INFINITY;
            for (Double2D p : points) {
                closest = Math.min(closest, other.closestDistance(p));
            }
            for (Double2D p : other.points) {
                closest = Math.min(closest, this.closestDistance(p));
            }
            return closest;
        }

        public Pair<Double, Segment> closestSegment(Double2D testPoint) {
            double min = Double.POSITIVE_INFINITY;
            Segment s = null;
            for (Segment seg : segments) {
                double d = GeomUtils.distToSegment(testPoint, seg.start, seg.end);
                if (d < min) {
                    min = d;
                    s = seg;
                }
            }
            return Pair.of(min, s);
        }

        public boolean isInsideBB(Double2D point) {
            return point.x > boundingBox.getLeft().x
                    && point.y > boundingBox.getLeft().y
                    && point.x < boundingBox.getRight().x
                    && point.y < boundingBox.getRight().y;
        }

        public Multiline add(Double2D pos) {
            Double2D[] newPoints = new Double2D[points.length];
            for (int i = 0; i < points.length; i++) {
                newPoints[i] = points[i].add(pos);
            }
            Segment[] newSegments = new Segment[segments.length];
            for (int i = 0; i < segments.length; i++) {
                newSegments[i] = new Segment(segments[i].start.add(pos), segments[i].end.add(pos));
            }
            Multiline newPoly = new Multiline(newSegments, newPoints, Pair.of(boundingBox.getLeft().add(pos),
                    boundingBox.getRight().add(pos)), center.add(pos), width, height);
            return newPoly;
        }

        public Shape buildShape() {
            GeneralPath path = new GeneralPath();
            for (Segment seg : segments) {
                path.moveTo(seg.start.x, seg.start.y);
                path.lineTo(seg.end.x, seg.end.y);
            }
            return path;
        }

    }

    /**
     *
     * @author jorge
     */
    public static class Segment {

        public final Double2D start;
        public final Double2D end;

        public Segment(Double2D start, Double2D end) {
            super();
            this.start = start;
            this.end = end;
        }

        public Segment(double startX, double startY, double endX, double endY) {
            super();
            this.start = new Double2D(startX, startY);
            this.end = new Double2D(endX, endY);
        }
    }

}
