package com.bawnorton.randoassistant.util;

import com.bawnorton.randoassistant.graph.LootTableGraph.Vertex;

import java.util.*;

public class Line extends LinkedList<Vertex> {
    public static final Line EMPTY = new Line(Collections.emptyList());
    HashSet<Vertex> quickContains = new HashSet<>();

    private Line(List<Vertex> vertices) {
        super(vertices);
        quickContains.addAll(vertices);
    }

    public static LineBuilder builder() {
        return new LineBuilder();
    }

    public boolean contains(Vertex vertex) {
        return quickContains.contains(vertex);
    }

    @Override
    public String toString() {
        return "Line{" + super.toString() + "}";
    }

    public static class LineBuilder {
        Set<Line> lines = new HashSet<>();

        public LineBuilder addLines(Vertex from) {
            Set<List<Vertex>> rootPaths = getRootPaths(from);
            for (List<Vertex> rootPath : rootPaths) {
                if(rootPath.isEmpty()) continue;
                lines.add(new Line(rootPath));
            }
            return this;
        }

        private static Set<List<Vertex>> getRootPaths(Vertex from) {
            Set<Vertex> visited = new HashSet<>();
            List<Vertex> path = new ArrayList<>();
            Set<List<Vertex>> rootPaths = new HashSet<>();
            getRootPathsHelper(from, visited, path, rootPaths);
            return rootPaths;
        }

        private static void getRootPathsHelper(Vertex node, Set<Vertex> visited, List<Vertex> path, Set<List<Vertex>> rootPaths) {
            path.add(node);
            visited.add(node);

            if (node.getImmediateParents().isEmpty()) {
                rootPaths.add(new ArrayList<>(path));
            } else {
                for (Vertex parent : node.getImmediateParents()) {
                    if (!visited.contains(parent)) {
                        getRootPathsHelper(parent, visited, new ArrayList<>(path), rootPaths);
                    }
                }
            }
        }

        public Set<Line> build() {
            return lines;
        }
    }
}
