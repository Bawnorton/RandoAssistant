package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.List;
import java.util.*;

// directed cyclic graph of loot tables and interactions
public class TrackingGraph extends SimpleDirectedGraph<TrackingGraph.Vertex, TrackingGraph.Edge> implements Iterable<TrackingGraph.Vertex> {
    private static TrackingGraph INSTANCE;

    private final Set<Edge> EDGES = new HashSet<>();
    private final Set<Vertex> VERTICES = Sets.newTreeSet(Comparator.comparing(Vertex::getIdentifier));
    private final Map<Identifier, Vertex> VERTEX_MAP = new HashMap<>();

    private TrackingGraph() {
        super(Edge.class);
    }

    public static TrackingGraph getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TrackingGraph();
        }
        return INSTANCE;
    }

    public Set<Edge> getEdges() {
        return EDGES;
    }

    public Set<Vertex> getVertices() {
        return VERTICES;
    }

    /**
     * Adds the given vertex to this graph.
     * @param vertex vertex to be added to this graph.
     * @return true if the vertex was added, false otherwise.
     */
    @Override
    public boolean addVertex(Vertex vertex) {
        if (super.addVertex(vertex)) {
            VERTICES.add(vertex);
            if (vertex.getIdentifier() != null) {
                VERTEX_MAP.put(vertex.getIdentifier(), vertex);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the vertex with the given identifier.
     * @param identifier the identifier of the vertex to get.
     * @return the vertex.
     */
    public Vertex getVertex(Identifier identifier) {
        return VERTEX_MAP.get(identifier);
    }

    /**
     * Gets the vertex with the given identifier.
     * @param item the item of the vertex to get.
     * @return the vertex.
     */
    public Vertex getVertex(Item item) {
        return getVertex(Registries.ITEM.getId(item));
    }

    /**
     * Gets the vertex with the given identifier, or creates it if it doesn't exist.
     * @param vertex the vertex to get or create.
     * @return the vertex.
     */
    public Vertex getOrCreateVertex(Vertex vertex) {
        Vertex existingVertex = getVertex(vertex.getIdentifier());
        if (existingVertex != null) {
            return existingVertex;
        }
        addVertex(vertex);
        return vertex;
    }

    /**
     * Adds an edge from the given source to the given target.
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     * @return the edge if added, null otherwise.
     */
    @Override
    public Edge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        try {
            Edge edge = super.addEdge(sourceVertex, targetVertex);
            if(edge == null) return null;
            EDGES.add(edge);
            return edge;
        } catch (IllegalArgumentException e) {
            if(e.getMessage().contains("loops not allowed")) RandoAssistant.LOGGER.warn("Skipping Edge from " + sourceVertex + " to " + targetVertex + " because it is self-looping");
            else RandoAssistant.LOGGER.error("Failed to add Edge from " + sourceVertex + " to " + targetVertex, e);
            return null;
        }
    }

    /**
     * Returns a new Edge from the given source to the given target.
     * @param sources the source vertices
     * @param targets the target vertices
     */
    public void addEdges(Iterable<Identifier> sources, Iterable<Identifier> targets) {
        for (Identifier source : sources) {
            Vertex sourceVertex = getOrCreateVertex(new Vertex(source));
            for (Identifier target : targets) {
                addEdge(sourceVertex, getOrCreateVertex(new Vertex(target)));
            }
        }
    }

    /**
     * Adds an edge from the given source to the given target.
     * @param source the source vertex
     * @param targets the target vertices
     */
    public void addEdges(Identifier source, Iterable<Identifier> targets) {
        Vertex sourceVertex = getOrCreateVertex(new Vertex(source));
        for (Identifier target : targets) {
            addEdge(sourceVertex, getOrCreateVertex(new Vertex(target)));
        }
    }

    /**
     * Returns the set of vertices that are the roots of the graph.
     * @return the set of vertices that are the roots of the graph
     */
    public Set<Vertex> getRoots() {
        Set<Vertex> roots = new HashSet<>();
        for (Vertex vertex : vertexSet()) {
            if (incomingEdgesOf(vertex).isEmpty()) {
                roots.add(vertex);
            }
        }
        return roots;
    }

    /**
     * Returns the set of vertices that are the roots of the given vertex.
     * @param vertex the vertex to find the roots of
     * @return the set of vertices that are the roots of the given vertex
     */
    public Set<Vertex> getRootsOf(Vertex vertex) {
        Set<Vertex> roots = new HashSet<>();
        for(Vertex root : getRoots()) {
            if(getChildVertices(root).contains(vertex)) {
                roots.add(root);
            }
        }
        return roots;
    }

    /**
     * Checks if the given vertex is a root of the graph.
     * @param vertex the vertex to check
     * @return true if the given vertex is a root of the graph, false otherwise
     */
    public boolean isRoot(Vertex vertex) {
        return getRoots().contains(vertex);
    }

    /**
     * Returns the set of vertices that are the parents of the given vertex.
     * @param vertex the vertex to find the parents of
     * @param visited the set of vertices that have already been visited
     * @return the set of vertices that are the parents of the given vertex
     */
    private Set<Vertex> getParentVertices(Vertex vertex, Set<Vertex> visited) {
        Set<Vertex> parents = new HashSet<>();
        for (Edge edge : incomingEdgesOf(vertex)) {
            Vertex parent = getEdgeSource(edge);
            if (!visited.contains(parent)) {
                parents.add(parent);
                visited.add(parent);
                parents.addAll(getParentVertices(parent, visited));
            }
        }
        return parents;
    }

    /**
     * Returns the set of vertices that are the parents of the given vertex.
     * @param vertex the vertex to find the parents of
     * @return the set of vertices that are the parents of the given vertex
     */
    public Set<Vertex> getParentVertices(Vertex vertex) {
        return getParentVertices(vertex, new HashSet<>());
    }

    /**
     * Returns the set of vertices that are the children of the given vertex.
     * @param vertex the vertex to find the children of
     * @param visited the set of vertices that have already been visited
     * @return the set of vertices that are the children of the given vertex
     */
    private Set<Vertex> getChildVertices(Vertex vertex, Set<Vertex> visited) {
        Set<Vertex> children = new HashSet<>();
        for (Edge edge : outgoingEdgesOf(vertex)) {
            Vertex child = getEdgeTarget(edge);
            if (child != null && !visited.contains(child)) {
                children.add(child);
                visited.add(child);
                children.addAll(getChildVertices(child, visited));
            }
        }
        return children;
    }

    /**
     * Returns the set of vertices that are the children of the given vertex.
     * @param vertex the vertex to find the children of
     * @return the set of vertices that are the children of the given vertex
     */
    public Set<Vertex> getChildVertices(Vertex vertex) {
        return getChildVertices(vertex, new HashSet<>());
    }

    @NotNull @Override
    public Iterator<Vertex> iterator() {
        return VERTICES.iterator();
    }

    public class Vertex implements grapher.graph.elements.Vertex {
        private final Identifier identifier;
        private Item item;

        public Vertex(Identifier identifier) {
            this.identifier = identifier;
            if(Registries.ITEM.containsId(identifier)) {
                this.item = Registries.ITEM.get(identifier);
            }
        }

        @Override
        public Dimension getSize() {
            return new Dimension(0, 0);
        }

        @Override
        public Object getContent() {
            return getIdentifier();
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public Item getItem() {
            if(item == null) {
                throw new IllegalStateException("Vertex is a non-item vertex");
            }
            return item;
        }

        public List<Vertex> getImmediateParents() {
            Set<Edge> edges = incomingEdgesOf(this);
            List<Vertex> parents = new ArrayList<>();
            for (Edge edge : edges) {
                parents.add(edge.getOrigin());
            }
            return parents;
        }

        @Override
        public String toString() {
            return "Vertex{id=" + identifier + '}';
        }
    }
    
    public static class Edge implements grapher.graph.elements.Edge<Vertex> {
        private final Vertex source;
        private final Vertex target;
        
        // required for jgrapht
        @SuppressWarnings("unused")
        public Edge() {
            this(null, null);
        }
        
        public Edge(Vertex source, Vertex target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public Vertex getOrigin() {
            return source;
        }

        @Override
        public Vertex getDestination() {
            return target;
        }
    }
}


