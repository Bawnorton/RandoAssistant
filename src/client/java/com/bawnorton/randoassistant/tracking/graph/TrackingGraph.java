package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.client.IntrusiveEdgeAccessor;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.*;
import java.util.List;

// directed cyclic graph of loot tables and interactions
public class TrackingGraph extends SimpleDirectedGraph<TrackingGraph.Vertex, DefaultEdge> {
    private static TrackingGraph INSTANCE;

    private final Set<DefaultEdge> EDGES = new HashSet<>();
    private final Set<Vertex> VERTICES = new HashSet<>();
    private final Map<Identifier, Vertex> VERTEX_MAP = new HashMap<>();

    public TrackingGraph() {
        super(DefaultEdge.class);
    }

    public static TrackingGraph getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TrackingGraph();
        }
        return INSTANCE;
    }

    public Set<DefaultEdge> getEdges() {
        return EDGES;
    }

    public Set<Vertex> getVertices() {
        return VERTICES;
    }

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

    public Vertex getVertex(Identifier identifier) {
        return VERTEX_MAP.get(identifier);
    }

    public Vertex getOrCreateVertex(Vertex vertex) {
        Vertex existingVertex = getVertex(vertex.getIdentifier());
        if (existingVertex != null) {
            return existingVertex;
        }
        addVertex(vertex);
        return vertex;
    }

    @Override
    public DefaultEdge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        try {
            if (super.addEdge(sourceVertex, targetVertex) == null) {
                return null;
            }
            DefaultEdge edge = of(sourceVertex, targetVertex);
            EDGES.add(edge);
            return edge;
        } catch (IllegalArgumentException e) {
            if(e.getMessage().contains("loops not allowed")) RandoAssistant.LOGGER.warn("Skipping DefaultEdge from " + sourceVertex + " to " + targetVertex + " because it is self-looping");
            else RandoAssistant.LOGGER.error("Failed to add DefaultEdge from " + sourceVertex + " to " + targetVertex, e);
            return null;
        }
    }

    public void addEdges(Iterable<Identifier> sources, Iterable<Identifier> targets) {
        for (Identifier source : sources) {
            Vertex sourceVertex = getOrCreateVertex(new Vertex(source));
            for (Identifier target : targets) {
                addEdge(sourceVertex, getOrCreateVertex(new Vertex(target)));
            }
        }
    }

    public void addEdges(Identifier source, Iterable<Identifier> targets) {
        Vertex sourceVertex = getOrCreateVertex(new Vertex(source));
        for (Identifier target : targets) {
            addEdge(sourceVertex, getOrCreateVertex(new Vertex(target)));
        }
    }

    private Set<Vertex> getParentVertices(Vertex vertex, Set<Vertex> visited) {
        Set<Vertex> parents = new HashSet<>();
        for (DefaultEdge edge : incomingEdgesOf(vertex)) {
            Vertex parent = getSource(edge);
            if (!visited.contains(parent)) {
                parents.add(parent);
                visited.add(parent);
                parents.addAll(getParentVertices(parent, visited));
            }
        }
        return parents;
    }

    public Set<Vertex> getParentVertices(Vertex vertex) {
        return getParentVertices(vertex, new HashSet<>());
    }

    private Set<Vertex> getChildVertices(Vertex vertex, Set<Vertex> visited) {
        Set<Vertex> children = new HashSet<>();
        for (DefaultEdge edge : outgoingEdgesOf(vertex)) {
            Vertex child = getTarget(edge);
            if (!visited.contains(child)) {
                children.add(child);
                visited.add(child);
                children.addAll(getChildVertices(child, visited));
            }
        }
        return children;
    }

    public Set<Vertex> getChildVertices(Vertex vertex) {
        return getChildVertices(vertex, new HashSet<>());
    }

    private DefaultEdge of(Vertex source, Vertex target) {
        DefaultEdge edge = new DefaultEdge();
        setSource(edge, source);
        setTarget(edge, target);
        return edge;
    }

    private Vertex getSource(DefaultEdge edge) {
        return (Vertex) ((IntrusiveEdgeAccessor) edge).getSource();
    }

    private Vertex getTarget(DefaultEdge edge) {
        return (Vertex) ((IntrusiveEdgeAccessor) edge).getTarget();
    }

    private void setSource(DefaultEdge edge, Vertex source) {
        ((IntrusiveEdgeAccessor) edge).setSource(source);
    }

    private void setTarget(DefaultEdge edge, Vertex target) {
        ((IntrusiveEdgeAccessor) edge).setTarget(target);
    }

    public class Vertex implements grapher.graph.elements.Vertex {
        Identifier identifier;

        public Vertex(Identifier identifier) {
            this.identifier = identifier;
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

        public Text getTooltop() {
            return Text.of(identifier.toString());
        }

        public List<Vertex> getImmediateParents() {
            Set<DefaultEdge> edges = incomingEdgesOf(this);
            List<Vertex> parents = new ArrayList<>();
            for (DefaultEdge edge : edges) {
                parents.add(getSource(edge));
            }
            return parents;
        }
    }
}


