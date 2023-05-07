package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.*;

// directed cyclic graph of loot tables and interactions
public class TrackingGraph extends SimpleDirectedGraph<TrackingGraph.Vertex, TrackingGraph.Edge> implements Iterable<TrackingGraph.Vertex> {
    private final Set<Vertex> VERTICES = Sets.newTreeSet(Comparator.comparing(Vertex::getIdentifier));
    private final Map<Identifier, Vertex> VERTEX_MAP = new HashMap<>();

    private Set<Vertex> cachedRoots = Sets.newHashSet();
    private boolean changed = false;

    public TrackingGraph() {
        super(Edge.class);
    }

    /**
     * Creates a subgraph of this graph containing only the given vertices.
     *
     * @param vertices the vertices to include in the subgraph.
     * @return the subgraph.
     */
    public TrackingGraph createSubGraph(Collection<Vertex> vertices) {
        TrackingGraph subGraph = new TrackingGraph();

        edgeSet().forEach(edge -> {
            if (vertices.contains(edge.getOrigin()) && vertices.contains(edge.getDestination())) {
                subGraph.addVertex(edge.getOrigin());
                subGraph.addVertex(edge.getDestination());
                subGraph.addEdge(edge.getOrigin(), edge.getDestination());
            }
        });

        return subGraph;
    }

    /**
     * Adds the given vertex to this graph.
     *
     * @param vertex vertex to be added to this graph.
     * @return true if the vertex was added, false otherwise.
     */
    @Override
    public boolean addVertex(Vertex vertex) {
        changed = false;
        if (super.addVertex(vertex)) {
            VERTICES.add(vertex);
            changed = true;
            if (vertex.getIdentifier() != null) {
                VERTEX_MAP.put(vertex.getIdentifier(), vertex);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the vertex with the given identifier.
     *
     * @param identifier the identifier of the vertex to get.
     * @return the vertex.
     */
    public Vertex getVertex(Identifier identifier) {
        return VERTEX_MAP.get(identifier);
    }

    /**
     * Gets the vertex with the given identifier.
     *
     * @param item the item of the vertex to get.
     * @return the vertex.
     */
    public Vertex getVertex(Item item) {
        return getVertex(Registries.ITEM.getId(item));
    }


    /**
     * Gets the distance from the fromVertex vertex to the child vertex.
     *
     * @param fromVertex the fromVertex vertex, must be an ancestor of the child vertex.
     * @param child      the child vertex, must be a descendant of the fromVertex vertex.
     * @return the distance from the fromVertex vertex to the child vertex, or -1 if the child is not a descendant of the fromVertex vertex.
     */
    public int distanceToChild(Vertex fromVertex, Vertex child) {
        return distanceToChild(fromVertex, child, Sets.newHashSet());
    }

    private int distanceToChild(Vertex fromVertex, Vertex child, Set<Vertex> visited) {
        if (fromVertex == child) {
            return 0;
        }
        int distance = 0;
        for (Edge edge : outgoingEdgesOf(fromVertex)) {
            Vertex destination = edge.getDestination();
            if (visited.contains(destination)) {
                continue;
            }
            visited.add(destination);
            distance = Math.max(distance, distanceToChild(destination, child, visited));
        }
        return distance == 0 ? -1 : distance + 1;
    }


    /**
     * Gets the distance from the fromVertex vertex to the parent vertex.
     *
     * @param fromVertex the fromVertex vertex, must be a descendant of the parent vertex.
     * @param parent     the parent vertex, must be an ancestor of the fromVertex vertex.
     * @return the distance from the fromVertex vertex to the parent vertex, or -1 if the fromVertex is not a descendant of the parent vertex.
     */
    public int distanceToParent(Vertex fromVertex, Vertex parent) {
        return distanceToParent(fromVertex, parent, Sets.newHashSet());
    }

    private int distanceToParent(Vertex fromVertex, Vertex parent, Set<Vertex> visited) {
        if (fromVertex == parent) {
            return 0;
        }
        int distance = 0;
        for (Edge edge : incomingEdgesOf(fromVertex)) {
            Vertex origin = edge.getOrigin();
            if (visited.contains(origin)) {
                continue;
            }
            visited.add(origin);
            distance = Math.max(distance, distanceToParent(origin, parent, visited));
        }
        return distance == 0 ? -1 : distance + 1;
    }

    /**
     * Gets the vertex with the given identifier, or creates it if it doesn't exist.
     *
     * @param vertex the vertex to get or create.
     * @return the vertex.
     */
    public Vertex getOrCreateVertex(Vertex vertex) {
        Vertex existingVertex = getVertex(vertex.getIdentifier());
        if (existingVertex == null) {
            addVertex(vertex);
            return vertex;
        }
        if (!existingVertex.equals(vertex)) {
            throw new IllegalArgumentException("Vertex with identifier " + vertex.getIdentifier() + " already exists with different properties.");
        }
        return existingVertex;
    }

    @Override
    public boolean removeVertex(Vertex vertex) {
        changed = false;
        if (super.removeVertex(vertex)) {
            VERTICES.remove(vertex);
            changed = true;
            if (vertex.getIdentifier() != null) {
                VERTEX_MAP.remove(vertex.getIdentifier());
            }
            return true;
        }
        return false;
    }

    public void removeVertexAndParents(Identifier identifier) {
        Vertex vertex = getVertex(identifier);
        if (vertex == null) return;
        Set<Vertex> parents = getParentVertices(vertex);
        parents.add(vertex);
        parents.forEach(this::removeVertex);
    }

    /**
     * Adds an edge from the given origin to the given fromVertex.
     *
     * @param sourceVertex origin vertex of the edge.
     * @param targetVertex fromVertex vertex of the edge.
     * @return the edge if added, null otherwise.
     */
    @Override
    public Edge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        try {
            Edge edge = super.addEdge(sourceVertex, targetVertex);
            if (edge == null) {
                edge = new Edge();
            }
            edge.setOrigin(sourceVertex);
            edge.setDestination(targetVertex);
            return edge;
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("loops not allowed")) {
                RandoAssistant.LOGGER.error("Failed to add Edge from " + sourceVertex + " to " + targetVertex, e);
            }
            return null;
        }
    }

    /**
     * Returns a new Edge from the given origin to the given fromVertex.
     *
     * @param sources the origin vertices
     * @param targets the fromVertex vertices
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
     * Adds an edge from the given origin to the given fromVertex.
     *
     * @param source  the origin vertex
     * @param targets the fromVertex vertices
     */
    public void addEdges(Identifier source, Iterable<Identifier> targets) {
        Vertex sourceVertex = getOrCreateVertex(new Vertex(source));
        for (Identifier target : targets) {
            addEdge(sourceVertex, getOrCreateVertex(new Vertex(target)));
        }
    }

    /**
     * Returns the set of vertices that are the roots of the graph.
     *
     * @return the set of vertices that are the roots of the graph
     */
    public Set<Vertex> getRoots() {
        if (!changed) return cachedRoots;
        Set<Vertex> roots = new HashSet<>();
        for (Vertex vertex : vertexSet()) {
            if (incomingEdgesOf(vertex).isEmpty()) {
                roots.add(vertex);
            }
        }
        cachedRoots = roots;
        changed = false;
        return roots;
    }

    /**
     * Returns the set of vertices that are the roots of the given vertex.
     *
     * @param vertex the vertex to find the roots of
     * @return the set of vertices that are the roots of the given vertex
     */
    public Set<Vertex> getRootsOf(Vertex vertex) {
        Set<Vertex> roots = new HashSet<>();
        getParentVertices(vertex).forEach(parent -> {
            if (isRoot(parent)) {
                roots.add(parent);
            }
        });
        return roots;
    }

    /**
     * Checks if the given vertex is a root of the graph.
     * If a vertex is not a root it will have an item value, otherwise it may not.
     *
     * @param vertex the vertex to check
     * @return true if the given vertex is a root of the graph, false otherwise
     */
    public boolean isRoot(Vertex vertex) {
        return getRoots().contains(vertex);
    }

    /**
     * Returns the set of vertices that are the parents of the given vertex.
     *
     * @param vertex  the vertex to find the parents of
     * @param visited the set of vertices that have already been visited
     * @return the set of vertices that are the parents of the given vertex
     */
    private Set<Vertex> getParentVertices(Vertex vertex, Set<Vertex> visited, Set<Vertex> parents) {
        if (visited.contains(vertex) || !VERTICES.contains(vertex)) {
            return parents;
        }
        visited.add(vertex);
        Set<Edge> edgeSet = incomingEdgesOf(vertex);
        for (Edge edge : edgeSet) {
            Vertex parent = edge.getOrigin();
            if (parent == null) continue;
            parents.add(parent);
            getParentVertices(parent, visited, parents);
        }
        return parents;
    }

    /**
     * Returns the set of vertices that are the parents of the given vertex.
     *
     * @param vertex the vertex to find the parents of
     * @return the set of vertices that are the parents of the given vertex
     */
    public Set<Vertex> getParentVertices(Vertex vertex) {
        return getParentVertices(vertex, new HashSet<>(), new HashSet<>());
    }

    /**
     * Returns the set of vertices that are the children of the given vertex.
     *
     * @param vertex   the vertex to find the children of
     * @param visited  the set of vertices that have already been visited
     * @param children the set of vertices that are the children of the given vertex
     * @return the set of vertices that are the children of the given vertex
     */
    private Set<Vertex> getChildVertices(Vertex vertex, Set<Vertex> visited, Set<Vertex> children) {
        if (visited.contains(vertex)) {
            return children;
        }
        visited.add(vertex);
        for (Edge edge : outgoingEdgesOf(vertex)) {
            Vertex child = edge.getDestination();
            if (child == null) continue;
            children.add(child);
            getChildVertices(child, visited, children);
        }
        return children;
    }


    /**
     * Returns the set of vertices that are the children of the given vertex.
     *
     * @param vertex the vertex to find the children of
     * @return the set of vertices that are the children of the given vertex
     */
    public Set<Vertex> getChildVertices(Vertex vertex) {
        return getChildVertices(vertex, new HashSet<>(), new HashSet<>());
    }

    @NotNull
    @Override
    public Iterator<Vertex> iterator() {
        return VERTICES.iterator();
    }

    @Override
    public String toString() {
        return "Graph{" + "VERTICES=" + VERTICES + '}';
    }

    public static class Vertex implements grapher.graph.elements.Vertex {
        private final Identifier identifier;
        private Item item;

        public Vertex(Identifier identifier) {
            this.identifier = identifier;
            if (Registries.ITEM.containsId(identifier)) {
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
            if (item == null) {
                throw new IllegalStateException("Vertex is a non-item vertex");
            }
            return item;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vertex other) {
                return identifier.equals(other.identifier);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Vertex{id=" + identifier + '}';
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


