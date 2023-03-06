package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.thread.GraphTaskExecutor;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

// Directed cyclic graph ofItems loot tables
public class LootTableGraph extends SimpleDirectedGraph<LootTableGraph.Vertex, LootTableGraph.Edge> {
    private final Set<Edge> edges;
    private final Set<Vertex> vertices;
    private final Map<Item, Vertex> itemVertexMap;

    private final GraphTaskExecutor graphTaskExecutor;

    public LootTableGraph() {
        super(Edge.class);
        edges = new HashSet<>();
        vertices = new HashSet<>();
        itemVertexMap = new HashMap<>();
        graphTaskExecutor = new GraphTaskExecutor(this);
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
        graphTaskExecutor.markDrawTaskDirty();
        graphTaskExecutor.draw();
    }

    public void addLootTable(Block block, List<Item> items) {
        Vertex root = getOrCreateNode(new Vertex(block));
        for (Item item : items) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
        graphTaskExecutor.markDrawTaskDirty();
        graphTaskExecutor.draw();
    }

    public void addLootTable(Identifier lootTableId, List<Item> oldLootTable) {
        Vertex root = getOrCreateNode(new Vertex(lootTableId));
        for (Item item : oldLootTable) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
        graphTaskExecutor.markDrawTaskDirty();
        graphTaskExecutor.draw();
    }

    public void addInteraction(Set<Item> input, Set<Item> output) {
        for (Item outputItem : output) {
            Vertex root = getOrCreateNode(new Vertex(outputItem));
            for (Item item : input) {
                Vertex vertex = getOrCreateNode(new Vertex(item));
                addEdge(vertex, root);
                addVertex(vertex);
            }
        }
        graphTaskExecutor.markDrawTaskDirty();
        graphTaskExecutor.draw();
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

    private Set<Edge> getAssociatedEdges(Set<Vertex> vertices) {
        Set<Edge> associatedEdges = new HashSet<>();
        for (Edge edge : edges) {
            if (vertices.contains(edge.getOrigin()) && vertices.contains(edge.getDestination())) {
                associatedEdges.add(edge);
            }
        }
        return associatedEdges;
    }

    private Set<Vertex> getVerticesAssociatedWithVertex(Vertex vertex, boolean includeSelf) {
        Set<Vertex> vertices = new HashSet<>();
        if (includeSelf) vertices.add(vertex);
        vertices.addAll(vertex.getChildren());
        vertices.addAll(vertex.getParents());
        return vertices;
    }

    private Set<Edge> getEdgesAssociatedWithVertices(Set<Vertex> vertices) {
        return getAssociatedEdges(vertices);
    }

    private Set<Vertex> getImmediateParents(Vertex vertex) {
        Set<Vertex> parents = new HashSet<>();
        for (Edge edge : incomingEdgesOf(vertex)) {
            parents.add(getEdgeSource(edge));
        }
        return parents;
    }

    private Set<Vertex> getImmediateChildren(Vertex vertex) {
        Set<Vertex> children = new HashSet<>();
        for (Edge edge : outgoingEdgesOf(vertex)) {
            children.add(getEdgeTarget(edge));
        }
        return children;
    }

    private Set<Vertex> getImmediateVerticesAssociatedWithVertex(Vertex vertex, boolean includeSelf) {
        Set<Vertex> vertices = new HashSet<>();
        if (includeSelf) vertices.add(vertex);
        vertices.addAll(getImmediateChildren(vertex));
        vertices.addAll(getImmediateParents(vertex));
        return vertices;
    }

    public Set<Vertex> getLeaves() {
        Set<Vertex> leaves = new HashSet<>();
        for (Vertex vertex : vertexSet()) {
            if (outDegreeOf(vertex) == 0) {
                leaves.add(vertex);
            }
        }
        return leaves;
    }

    public Set<Vertex> getBranches() {
        Set<Vertex> branches = new HashSet<>();
        for (Vertex vertex : vertexSet()) {
            if (outDegreeOf(vertex) > 1) {
                branches.add(vertex);
            }
        }
        return branches;
    }

    public Vertex getVertex(Item item) {
        return itemVertexMap.get(item);
    }

    public GraphTaskExecutor getExecutor() {
        return graphTaskExecutor;
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

    public static class LootTableType {
        private final Block block;
        private final Item item;
        private final EntityType<?> entityType;
        private final Identifier lootTableId;

        private final boolean isBlock;
        private final boolean isItem;
        private final boolean isEntity;
        private final boolean isLootTable;

        private final Category category;

        public LootTableType(Block block, Item item, EntityType<?> entityType, Identifier lootTableId) {
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
            this.isLootTable = lootTableId != null;

            this.block = block;
            this.item = item;
            this.entityType = entityType;
            this.lootTableId = lootTableId;

            if (isBlock) {
                category = Category.BLOCK;
            } else if (isItem) {
                category = Category.ITEM;
            } else if (isEntity) {
                category = Category.ENTITY;
            } else if (isLootTable) {
                category = Category.LOOT_TABLE;
            } else {
                throw new IllegalArgumentException("Invalid LootTableType");
            }
        }

        public LootTableType(Block block) {
            this(block, null, null, null);
        }

        public LootTableType(Item item) {
            this(null, item, null, null);
        }

        public LootTableType(EntityType<?> entityType) {
            this(null, null, entityType, null);
        }

        public LootTableType(Identifier lootTableId) {
            this(null, null, null, lootTableId);
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

        public Identifier getLootTableId() {
            if (!isLootTable) return null;
            return lootTableId;
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
                } else if (isLootTable) {
                    return other.isLootTable && lootTableId.equals(other.lootTableId);
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
            } else if (isLootTable) {
                return lootTableId.toString().hashCode();
            }
            return 0;
        }

        @Override
        public String toString() {
            return "LootTableType{" + (isBlock ? "block=" + block : isItem ? "item=" + item : isEntity ? "entityType=" + entityType : null) + '}';
        }

        public enum Category {
            BLOCK,
            ITEM,
            ENTITY,
            LOOT_TABLE
        }
    }

    public class Vertex implements grapher.graph.elements.Vertex {
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

        public Vertex(Identifier id) {
            this.type = new LootTableType(id);
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

        public boolean isLootTable() {
            return type.isLootTable;
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

        public Identifier getLootTableId() {
            if (type.isLootTable) return type.getLootTableId();
            return null;
        }

        public LootTableType.Category getType() {
            return type.category;
        }

        @Override
        public Dimension getSize() {
            return new Dimension(16, 16);
        }

        @Override
        public Object getContent() {
            if (type.isBlock) {
                return type.getBlock();
            } else if (type.isItem) {
                return type.getItem();
            } else if (type.isEntity) {
                return type.getEntityType();
            } else {
                return type.getLootTableId();
            }
        }

        public Text getTooltip() {
            try {
                if (type.isBlock) {
                    assert type.getBlock() != null;
                    return type.getBlock().getName();
                } else if (type.isItem) {
                    assert type.getItem() != null;
                    return type.getItem().getName();
                } else if (type.isEntity) {
                    assert type.getEntityType() != null;
                    return type.getEntityType().getName();
                } else {
                    assert type.getLootTableId() != null;
                    String lootTableId = type.getLootTableId().toString();
                    String[] parts = lootTableId.split("/");
                    String tooltip = parts[parts.length - 1].replaceAll("_", " ");
                    tooltip = Arrays.stream(tooltip.split(" ")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
                    return Text.of(tooltip + " Chest");
                }
            } catch (Exception e) {
                return Text.of("§cUnknown");
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

        public Set<Vertex> getVerticesAssociatedWith(boolean includeSelf) {
            return LootTableGraph.this.getVerticesAssociatedWithVertex(this, includeSelf);
        }

        public Set<Edge> getEdgesAssociatedWithVertices(Set<Vertex> vertices) {
            return LootTableGraph.this.getEdgesAssociatedWithVertices(vertices);
        }

        public Set<Vertex> getImmediateParents() {
            return LootTableGraph.this.getImmediateParents(this);
        }

        public Set<Vertex> getImmediateChildren() {
            return LootTableGraph.this.getImmediateChildren(this);
        }

        public Set<Vertex> getImmediateVerticesAssociatedWith(boolean includeSelf) {
            return LootTableGraph.this.getImmediateVerticesAssociatedWithVertex(this, includeSelf);
        }

        public void highlightAsParent() {
            highlightAsParent = true;
            graphTaskExecutor.highlight(this);
        }

        public void unhighlightAsParent() {
            highlightAsParent = false;
        }

        public boolean isHighlightedAsParent() {
            return highlightAsParent;
        }

        public void highlightAsChild() {
            highlightAsChild = true;
            graphTaskExecutor.highlight(this);
        }

        public void unhighlightAsChild() {
            highlightAsChild = false;
        }

        public boolean isHighlightedAsChild() {
            return highlightAsChild;
        }

        public void highlightAsTarget() {
            highlightAsTarget = true;
            graphTaskExecutor.highlight(this);
        }

        public void unhighlightAsTarget() {
            highlightAsTarget = false;
        }

        public boolean isHighlightedAsTarget() {
            return highlightAsTarget;
        }

        public void highlightAsInteraction() {
            highlightAsInteraction = true;
            graphTaskExecutor.highlight(this);
        }

        public void unhighlightAsInteraction() {
            highlightAsInteraction = false;
        }

        public boolean isHighlightedAsInteraction() {
            return highlightAsInteraction;
        }

        public void highlightParents() {
            graphTaskExecutor.highlightParents(this);
        }

        public void highlightChildren() {
            graphTaskExecutor.highlightChildren(this);
        }

        public void unhighlightConnected() {
            graphTaskExecutor.unhighlightConnected();
        }
    }
}
