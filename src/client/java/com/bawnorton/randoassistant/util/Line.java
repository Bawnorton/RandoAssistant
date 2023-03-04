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

        private static Set<List<Vertex>> getRootPaths(Vertex from) {
            Map<Vertex, Integer> expectedVists = new HashMap<>();
            List<Vertex> path = new ArrayList<>();
            Set<List<Vertex>> rootPaths = new HashSet<>();
            getRootPathsHelper(from, expectedVists, path, rootPaths);
            return rootPaths;
        }

        private static void getRootPathsHelper(Vertex node, Map<Vertex, Integer> expectedVisits, List<Vertex> path, Set<List<Vertex>> rootPaths) {
            path.add(node);

            if (expectedVisits.containsKey(node)) {
                if (expectedVisits.get(node) <= 0) {
                    return;
                }
                expectedVisits.put(node, expectedVisits.get(node) - 1);
            }

            if (node.getImmediateParents().isEmpty()) {
                rootPaths.add(new ArrayList<>(path));
                return;
            }

            expectedVisits.putIfAbsent(node, node.getImmediateParents().size());

            if (node.getImmediateParents().size() >= 2) {
                for (Vertex parent : node.getImmediateParents()) {
                    List<Vertex> splitPath = new ArrayList<>(path);
                    getRootPathsHelper(parent, expectedVisits, splitPath, rootPaths);
                }
            } else {
                Vertex parent = node.getImmediateParents().iterator().next();
                getRootPathsHelper(parent, expectedVisits, path, rootPaths);
            }
        }

        public LineBuilder addLines(Vertex from) {
            Set<List<Vertex>> rootPaths = getRootPaths(from);
            for (List<Vertex> rootPath : rootPaths) {
                if (rootPath.isEmpty()) continue;
                lines.add(new Line(rootPath));
            }
            return this;
        }

        public Set<Line> build() {
            return lines;
        }
    }
}
