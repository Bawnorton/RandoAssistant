package com.bawnorton.randoassistant.util;

import java.awt.geom.Point2D;

public class Boundary {
    private final Point2D.Float[] points;

    public Boundary(Point2D.Float... points) {
        this.points = points;
    }

    public boolean contains(double x, double y) {
        return contains((float) x, (float) y);
    }

    public boolean contains(float x, float y) {
        return contains(new Point2D.Float(x, y));
    }

    public boolean contains(Point2D.Float point) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > point.y) != (points[j].y > point.y) && (point.x < (points[j].x - points[i].x) * (point.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }
}