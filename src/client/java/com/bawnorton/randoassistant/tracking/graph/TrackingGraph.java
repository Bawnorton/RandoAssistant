package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.google.common.collect.Maps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

// directed cyclic graph of loot tables and interactions
public class TrackingGraph extends SimpleDirectedGraph<TrackingGraph.Vertex, TrackingGraph.Edge> implements Iterable<TrackingGraph.Vertex> {
    private final TreeMap<Trackable<?>, Vertex> VERTEX_MAP = Maps.newTreeMap(Comparator.comparing(Trackable::getIdentifier));

    public TrackingGraph() {
        super(Edge.class);
    }

    @Override
    public boolean addVertex(Vertex vertex) {
        if(super.addVertex(vertex)) {
            VERTEX_MAP.put(vertex.getContent(), vertex);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeVertex(Vertex vertex) {
        if(super.removeVertex(vertex)) {
            VERTEX_MAP.remove(vertex.getContent());
            return true;
        }
        return false;
    }

    public void add(Trackable<?> trackable) {
        addVertex(new Vertex(trackable));
    }

    public void connect(Trackable<?> source, Trackable<?> destination) {
        Vertex sourceVertex = getVertex(source);
        if(sourceVertex == null) {
            add(source);
            sourceVertex = getVertex(source);
        }
        Vertex destinationVertex = getVertex(destination);
        if(destinationVertex == null) {
            add(destination);
            destinationVertex = getVertex(destination);
        }
        try {
            addEdge(sourceVertex, destinationVertex);
        } catch (IllegalArgumentException ignored) {
            // silently ignore self-looping edges
        }
    }

    public boolean contains(Trackable<?> trackable) {
        return VERTEX_MAP.containsKey(trackable);
    }

    public Vertex getVertex(Trackable<?> trackable) {
        return VERTEX_MAP.get(trackable);
    }

    public Edge getEdge(Trackable<?> source, Trackable<?> destination) {
        return getEdge(getVertex(source), getVertex(destination));
    }

    public Set<Vertex> getRoots() {
        return vertexSet().stream().filter(vertex -> inDegreeOf(vertex) == 0).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Iterator<Vertex> iterator() {
        return vertexSet().iterator();
    }

    public int distanceBetween(Vertex vertex1, Vertex vertex2) {
        int distance = 0;
        if(vertex1.equals(vertex2)) return distance;
        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();
        queue.add(vertex1);
        while(!queue.isEmpty()) {
            Vertex vertex = queue.poll();
            if(vertex.equals(vertex2)) return distance;
            visited.add(vertex);
            for(Edge edge : outgoingEdgesOf(vertex)) {
                Vertex destination = getEdgeTarget(edge);
                if(!visited.contains(destination)) {
                    queue.add(destination);
                }
            }
            distance++;
        }
        return -1;
    }

    public static class Vertex implements grapher.graph.elements.Vertex {
        private final Trackable<?> content;

        public Vertex(Trackable<?> trackable) {
            this.content = trackable;
        }

        public Trackable<?> getContent() {
            return content;
        }

        public Identifier getIdentifier() {
            return content.getIdentifier();
        }

        @Override
        public Dimension getSize() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vertex other) {
                return content.equals(other.content);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Vertex{" + content + '}';
        }
    }

    public static class Edge implements grapher.graph.elements.Edge<Vertex> {
        private Vertex origin;
        private Vertex destination;

        // required for jgrapht
        public Edge() {
            this(null, null);
        }

        public Edge(Vertex origin, Vertex destination) {
            this.origin = origin;
            this.destination = destination;
        }

        @Override
        public Vertex getOrigin() {
            return origin;
        }

        public void setOrigin(Vertex origin) {
            this.origin = origin;
        }

        @Override
        public Vertex getDestination() {
            return destination;
        }

        public void setDestination(Vertex destination) {
            this.destination = destination;
        }

        @Override
        public String toString() {
            return "Edge{" + origin + " -> " + destination + '}';
        }
    }
}


