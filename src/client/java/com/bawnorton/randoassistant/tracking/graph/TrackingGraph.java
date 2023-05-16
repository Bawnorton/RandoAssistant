package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.common.collect.Maps;
import grapher.graph.drawing.Drawing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

// directed cyclic graph of loot tables and interactions
public class TrackingGraph extends SimpleDirectedGraph<TrackingGraph.Vertex, TrackingGraph.Edge> implements Iterable<TrackingGraph.Vertex> {
    private final HashMap<Identifier, Vertex> VERTEX_MAP;
    private final GraphDrawer drawer;

    public TrackingGraph() {
        super(Edge.class);
        VERTEX_MAP = Maps.newHashMap();
        drawer = new GraphDrawer(this);
    }

    public Drawing<Vertex, Edge> draw() {
        return drawer.draw();
    }

    public void markDirty() {
        drawer.markDirty();
    }

    public boolean isDirty() {
        return drawer.isDirty();
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

    @Override
    public Edge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        Edge edge = super.addEdge(sourceVertex, targetVertex);
        if(edge != null) {
            edge.setOrigin(sourceVertex);
            edge.setDestination(targetVertex);
        }
        return edge;
    }

    public void add(Identifier identifier) {
        addVertex(new Vertex(identifier));
    }

    public void connect(Identifier source, Identifier destination) {
        if(!contains(source)) add(source);
        if(!contains(destination)) add(destination);
        Vertex sourceVertex = getVertex(source);
        Vertex destinationVertex = getVertex(destination);
        try {
            addEdge(sourceVertex, destinationVertex);
        } catch (IllegalArgumentException e) {
            if(!e.getMessage().equals("loops not allowed")) {
                RandoAssistant.LOGGER.error("Error connecting " + source + " to " + destination, e);
            }
        }
    }

    public void removeOutgoingEdges(Identifier target) {
        Vertex vertex = getVertex(target);
        Set<Edge> edges = outgoingEdgesOf(vertex);
        removeAllEdges(edges);
    }

    public boolean contains(Identifier identifier) {
        return VERTEX_MAP.containsKey(identifier);
    }

    @NotNull
    public Vertex getVertex(Identifier identifier) {
        if(!contains(identifier)) throw new IllegalArgumentException("Identifier " + identifier + " not found");
        return VERTEX_MAP.get(identifier);
    }

    public Set<Vertex> getRoots() {
        return vertexSet().stream().filter(vertex -> inDegreeOf(vertex) == 0).collect(Collectors.toSet());
    }


    public Set<Vertex> getChildren(Identifier identifier) {
        return getChildren(getVertex(identifier), new HashSet<>(), new HashSet<>());
    }

    private Set<Vertex> getChildren(Vertex parent, Set<Vertex> children, Set<Vertex> visited) {
        if(visited.contains(parent)) return children;
        visited.add(parent);
        for(Edge edge : outgoingEdgesOf(parent)) {
            Vertex child = getEdgeTarget(edge);
            children.add(child);
            getChildren(child, children, visited);
        }
        return children;
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
        private final Identifier content;

        public Vertex(Identifier identifier) {
            this.content = identifier;
        }

        public Identifier getContent() {
            return content;
        }

        public Identifier getIdentifier() {
            return getContent();
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
        public int hashCode() {
            return content.hashCode();
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
        @SuppressWarnings("unused")
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


