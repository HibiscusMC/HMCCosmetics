package com.hibiscusmc.hmccosmetics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Octree<T> {

    private static final int MAX_CAPACITY = 8;
    private BoundingBox boundary;
    private List<Entry> points;
    private Octree<T>[] children;
    private boolean isDivided;

    public Octree(BoundingBox boundary) {
        this.boundary = boundary;
        this.points = new CopyOnWriteArrayList<>();
        this.isDivided = false;
    }

    public boolean insert(Point3D position, T data) {
        if (!boundary.contains(position)) {
            return false;
        }

        if (points.size() < MAX_CAPACITY && !isDivided) {
            points.add(new Entry(position, data));
            return true;
        }

        if (!isDivided) {
            subdivide();
        }

        for (Octree<T> child : children) {
            if (child.insert(position, data)) {
                return true;
            }
        }

        return false;
    }

    public boolean remove(Point3D point, T player) {
        if (!boundary.contains(point)) {
            return false;
        }

        if (points.size() < MAX_CAPACITY && !isDivided) {
            points.removeIf(entry -> entry.position.equals(point) && entry.data.equals(player));
            return true;
        }

        if (!isDivided) {
            return false;
        }

        for (Octree<T> child : children) {
            if (child.remove(point, player)) {
                return true;
            }
        }

        return false;
    }

    public List<T> queryRange(BoundingBox range) {
        List<T> found = new ArrayList<>();

        if (!boundary.intersects(range)) {
            return found;
        }

        for (Entry entry : points) {
            if (range.contains(entry.position)) {
                found.add(entry.data);
            }
        }

        if (isDivided) {
            for (Octree<T> child : children) {
                found.addAll(child.queryRange(range));
            }
        }

        return found;
    }

    private void subdivide() {
        double x = boundary.center.x;
        double y = boundary.center.y;
        double z = boundary.center.z;
        double newHalfSize = boundary.halfSize / 2.0;

        children = new Octree[8];
        children[0] = new Octree<>(new BoundingBox(new Point3D(x - newHalfSize, y - newHalfSize, z - newHalfSize), newHalfSize));
        children[1] = new Octree<>(new BoundingBox(new Point3D(x + newHalfSize, y - newHalfSize, z - newHalfSize), newHalfSize));
        children[2] = new Octree<>(new BoundingBox(new Point3D(x - newHalfSize, y + newHalfSize, z - newHalfSize), newHalfSize));
        children[3] = new Octree<>(new BoundingBox(new Point3D(x + newHalfSize, y + newHalfSize, z - newHalfSize), newHalfSize));
        children[4] = new Octree<>(new BoundingBox(new Point3D(x - newHalfSize, y - newHalfSize, z + newHalfSize), newHalfSize));
        children[5] = new Octree<>(new BoundingBox(new Point3D(x + newHalfSize, y - newHalfSize, z + newHalfSize), newHalfSize));
        children[6] = new Octree<>(new BoundingBox(new Point3D(x - newHalfSize, y + newHalfSize, z + newHalfSize), newHalfSize));
        children[7] = new Octree<>(new BoundingBox(new Point3D(x + newHalfSize, y + newHalfSize, z + newHalfSize), newHalfSize));

        isDivided = true;
    }

    public static class Point3D {
        double x, y, z;

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class BoundingBox {
        Point3D center;
        double halfSize;

        public BoundingBox(Point3D center, double halfSize) {
            this.center = center;
            this.halfSize = halfSize;
        }

        public boolean contains(Point3D point) {
            return Math.abs(point.x - center.x) <= halfSize &&
                Math.abs(point.y - center.y) <= halfSize &&
                Math.abs(point.z - center.z) <= halfSize;
        }

        public boolean intersects(BoundingBox other) {
            return Math.abs(center.x - other.center.x) <= halfSize + other.halfSize &&
                Math.abs(center.y - other.center.y) <= halfSize + other.halfSize &&
                Math.abs(center.z - other.center.z) <= halfSize + other.halfSize;
        }
    }

    private class Entry {
        Point3D position;
        T data;

        Entry(Point3D position, T data) {
            this.position = position;
            this.data = data;
        }
    }
}