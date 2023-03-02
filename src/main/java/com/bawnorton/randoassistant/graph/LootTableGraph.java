package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.config.Config;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.List;
import java.util.*;

// Directed cyclic graph of loot tables
public class LootTableGraph extends SimpleDirectedGraph<LootTableGraph.Vertex, LootTableGraph.Edge> {
    private final Set<Edge> edges;
    private final Set<Vertex> vertices;
    private final Map<Item, Vertex> itemVertexMap;
    private final GraphDrawer graphDrawer;

    public LootTableGraph() {
        super(Edge.class);
        edges = new HashSet<>();
        vertices = new HashSet<>();
        itemVertexMap = new HashMap<>();
        graphDrawer = new GraphDrawer(this);
    }

    @Override
    public Edge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        try {
            if (super.addEdge(sourceVertex, targetVertex) == null) {
                return null;
            }
            Edge edge = new Edge(sourceVertex, targetVertex);
            edges.add(edge);
            return edge;
        } catch (IllegalArgumentException e) {
            RandoAssistant.LOGGER.warn("Skipping edge from " + sourceVertex + " to " + targetVertex + " because it is self-looping");
            return null;
        }
    }

    @Override
    public boolean addVertex(Vertex vertex) {
        if (super.addVertex(vertex)) {
            vertices.add(vertex);
            if (vertex.getItem() != null) {
                itemVertexMap.put(vertex.getItem(), vertex);
            }
            return true;
        }
        return false;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public Set<Vertex> getVerticesAssociatedWithVertex(Vertex vertex) {
        Set<Vertex> vertices = new HashSet<>();
        vertices.add(vertex);
        vertices.addAll(vertex.getChildren());
        vertices.addAll(vertex.getParents());
        return vertices;
    }

    public Set<Edge> getEdgesAssociatedWithVertices(Set<Vertex> vertices) {
        return getAssociatedEdges(vertices);
    }

    private Vertex getOrCreateNode(Vertex node) {
        for (Vertex vertex : vertexSet()) {
            if (vertex.equals(node)) {
                return vertex;
            }
        }
        addVertex(node);
        return node;
    }

    public void addLootTable(EntityType<?> entityType, List<Item> items) {
        Vertex root = getOrCreateNode(new Vertex(entityType));
        for (Item item : items) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
        graphDrawer.updateDrawing();
    }

    public void addLootTable(Block block, List<Item> items) {
        Vertex root = getOrCreateNode(new Vertex(block));
        for (Item item : items) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
        graphDrawer.updateDrawing();
    }

    public void addCraftingRecipe(Item item, List<Item> ingredients) {
        Vertex root = getOrCreateNode(new Vertex(item));
        for (Item ingredient : ingredients) {
            Vertex vertex = getOrCreateNode(new Vertex(ingredient));
            addEdge(vertex, root);
            addVertex(vertex);
        }
        graphDrawer.updateDrawing();
    }

    private Set<Vertex> getParents(Vertex vertex, Set<Vertex> visited, Set<Vertex> parents, int depth, int maxDepth) {
        if (visited.contains(vertex) || depth > maxDepth) {
            return parents;
        }
        visited.add(vertex);
        Set<Edge> edgeSet = incomingEdgesOf(vertex);
        for (Edge edge : edgeSet) {
            Vertex parent = getEdgeSource(edge);
            parents.add(parent);
            getParents(parent, visited, parents, ++depth, maxDepth);
        }
        return parents;
    }

    public Set<Vertex> getParents(Vertex vertex, int depth) {
        return getParents(vertex, new HashSet<>(), new HashSet<>(), 0, depth);
    }

    private Set<Vertex> getChildren(Vertex vertex, Set<Vertex> visited, Set<Vertex> children, int depth, int maxDepth) {
        if (visited.contains(vertex) || depth > maxDepth) {
            return children;
        }
        visited.add(vertex);
        for (Edge edge : outgoingEdgesOf(vertex)) {
            Vertex child = getEdgeTarget(edge);
            children.add(child);
            getChildren(child, visited, children, ++depth, maxDepth);
        }
        return children;
    }

    public Set<Vertex> getChildren(Vertex vertex, int depth) {
        return getChildren(vertex, new HashSet<>(), new HashSet<>(), 0, depth);
    }

    public Set<Edge> getAssociatedEdges(Set<Vertex> vertices) {
        Set<Edge> associatedEdges = new HashSet<>();
        for (Edge edge : edges) {
            if (vertices.contains(edge.getOrigin()) && vertices.contains(edge.getDestination())) {
                associatedEdges.add(edge);
            }
        }
        return associatedEdges;
    }

    private Set<Vertex> getImmediateParents(Vertex vertex) {
        Set<Vertex> parents = new HashSet<>();
        for (Edge edge : incomingEdgesOf(vertex)) {
            parents.add(getEdgeSource(edge));
        }
        return parents;
    }

    public GraphDrawer getDrawer() {
        return graphDrawer;
    }

    public Vertex getVertex(Item item) {
        return itemVertexMap.get(item);
    }

    public static class Edge extends DefaultEdge implements grapher.graph.elements.Edge<Vertex> {
        private final Vertex origin;
        private final Vertex destination;

        public Edge(Vertex origin, Vertex destination) {
            this.origin = origin;
            this.destination = destination;
        }

        public Edge() {
            this.origin = null;
            this.destination = null;
        }

        @Override
        public Vertex getOrigin() {
            return origin;
        }

        @Override
        public Vertex getDestination() {
            return destination;
        }

        @Override
        public String toString() {
            return (origin != null ? origin.toString() : null) + " -> " + (destination != null ? destination.toString() : null);
        }
    }

    private static class LootTableType {
        private final Block block;
        private final Item item;
        private final EntityType<?> entityType;
        private final boolean isBlock;
        private final boolean isItem;
        private final boolean isEntity;

        public LootTableType(Block block, Item item, EntityType<?> entityType) {
            if (block == null && item != null) {
                block = Block.getBlockFromItem(item);
                if (block.asItem() == Items.AIR) block = null;
            } else if (block != null && item == null) {
                item = block.asItem();
                if (item == Items.AIR) item = null;
            }

            this.isBlock = block != null;
            this.isItem = item != null;
            this.isEntity = entityType != null;

            this.block = block;
            this.item = item;
            this.entityType = entityType;
        }

        public LootTableType(Block block) {
            this(block, null, null);
        }

        public LootTableType(Item item) {
            this(null, item, null);
        }

        public LootTableType(EntityType<?> entityType) {
            this(null, null, entityType);
        }

        public Block getBlock() {
            if (!isBlock) return null;
            return block;
        }

        public Item getItem() {
            if (!isItem) return null;
            return item;
        }

        public EntityType<?> getEntityType() {
            if (!isEntity) return null;
            return entityType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LootTableType other) {
                if (isBlock) {
                    return other.isBlock && block.equals(other.block);
                } else if (isItem) {
                    return other.isItem && item.equals(other.item);
                } else if (isEntity) {
                    return other.isEntity && entityType.equals(other.entityType);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (isBlock) {
                return Registries.BLOCK.getId(block).toString().hashCode();
            } else if (isItem) {
                return Registries.ITEM.getId(item).toString().hashCode();
            } else if (isEntity) {
                return Registries.ENTITY_TYPE.getId(entityType).toString().hashCode();
            }
            return 0;
        }

        @Override
        public String toString() {
            return "LootTableType{" + (isBlock ? "block=" + block : isItem ? "item=" + item : isEntity ? "entityType=" + entityType : null) + '}';
        }
    }

    public class Vertex implements grapher.graph.elements.Vertex {
        private static final Set<Vertex> highlighted = new HashSet<>();

        private final LootTableType type;
        private boolean highlightAsParent = false;
        private boolean highlightAsChild = false;
        private boolean highlightAsTarget = false;
        private boolean highlightAsInteraction = false;

        public Vertex(Block block) {
            this.type = new LootTableType(block);
        }

        public Vertex(EntityType<?> entityType) {
            this.type = new LootTableType(entityType);
        }

        public Vertex(Item item) {
            this.type = new LootTableType(item);
        }

        public boolean isBlock() {
            return type.isBlock;
        }

        public boolean isItem() {
            return type.isItem;
        }

        public boolean isEntity() {
            return type.isEntity;
        }

        public Item getItem() {
            if (type.isItem) return type.getItem();
            return null;
        }

        public Block getBlock() {
            if (type.isBlock) return type.getBlock();
            return null;
        }

        public EntityType<?> getEntityType() {
            if (type.isEntity) return type.getEntityType();
            return null;
        }

        @Override
        public Dimension getSize() {
            return new Dimension(16, 16);
        }

        @Override
        public Object getContent() {
            return type.isBlock ? type.getBlock() : type.isItem ? type.getItem() : type.getEntityType();
        }

        public Text getTooltip() {
            if (type.isBlock) {
                return Objects.requireNonNull(type.getBlock()).getName();
            } else if (type.isItem) {
                return Objects.requireNonNull(type.getItem()).getName();
            } else {
                return Objects.requireNonNull(type.getEntityType()).getName();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vertex other) {
                return type.equals(other.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "type=" + type +
                    '}';
        }

        public Set<Vertex> getParents() {
            return LootTableGraph.this.getParents(this, Config.getInstance().parentDepth);
        }

        public Set<Vertex> getChildren() {
            return LootTableGraph.this.getChildren(this, Config.getInstance().childDepth);
        }

        public Set<Vertex> getVerticesAssociatedWith() {
            return LootTableGraph.this.getVerticesAssociatedWithVertex(this);
        }

        public Set<Edge> getEdgesAssociatedWithVertices(Set<Vertex> vertices) {
            return LootTableGraph.this.getEdgesAssociatedWithVertices(vertices);
        }

        public Set<Vertex> getImmediateParents() {
            return LootTableGraph.this.getImmediateParents(this);
        }

        public void highlightAsParent() {
            highlightAsParent = true;
            highlighted.add(this);
        }

        public void unhighlightAsParent() {
            highlightAsParent = false;
        }

        public boolean isHighlightedAsParent() {
            return highlightAsParent;
        }

        public void highlightAsChild() {
            highlightAsChild = true;
            highlighted.add(this);
        }

        public void unhighlightAsChild() {
            highlightAsChild = false;
        }

        public boolean isHighlightedAsChild() {
            return highlightAsChild;
        }

        public void highlightAsTarget() {
            highlightAsTarget = true;
            highlighted.add(this);
        }

        public void unhighlightAsTarget() {
            highlightAsTarget = false;
        }

        public boolean isHighlightedAsTarget() {
            return highlightAsTarget;
        }

        public void highlightAsInteraction() {
            highlightAsInteraction = true;
            highlighted.add(this);
        }

        public void unhighlightAsInteraction() {
            highlightAsInteraction = false;
        }

        public boolean isHighlightedAsInteraction() {
            return highlightAsInteraction;
        }

        public void highlightParents() {
            getParents().forEach(Vertex::highlightAsParent);
            highlighted.forEach(node -> {
                List<Item> items = RandoAssistant.interactionMap.getIngredients(node.getItem());
                if (items != null) {
                    node.highlightAsInteraction();
                    items.forEach(item -> {
                        Vertex vertex = LootTableGraph.this.getVertex(item);
                        if (vertex != null && vertex.isHighlightedAsParent()) {
                            vertex.highlightAsInteraction();
                        }
                    });
                }
            });
        }

        public void highlightChildren() {
            getChildren().forEach(Vertex::highlightAsChild);
            highlighted.forEach(node -> {
                List<Item> items = RandoAssistant.interactionMap.getIngredients(node.getItem());
                if (items != null) {
                    node.highlightAsInteraction();
                    items.forEach(item -> {
                        Vertex vertex = LootTableGraph.this.getVertex(item);
                        if (vertex != null && vertex.isHighlightedAsChild()) {
                            vertex.highlightAsInteraction();
                        }
                    });
                }
            });
        }

        public void unhighlightConnected() {
            highlighted.forEach(vertex -> {
                vertex.unhighlightAsParent();
                vertex.unhighlightAsChild();
                vertex.unhighlightAsTarget();
                vertex.unhighlightAsInteraction();
            });
            highlighted.clear();
        }
    }
}
