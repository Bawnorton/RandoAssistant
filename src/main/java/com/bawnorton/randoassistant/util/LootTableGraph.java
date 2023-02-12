package com.bawnorton.randoassistant.util;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.*;
import java.util.List;

// Directed cyclic graph of loot tables
public class LootTableGraph extends SimpleDirectedGraph<LootTableGraph.Vertex, LootTableGraph.Edge> {
    private final List<Edge> edges;
    private final List<Vertex> vertices;

    public LootTableGraph() {
        super(Edge.class);
        edges = new ArrayList<>();
        vertices = new ArrayList<>();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Edge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        try {
            if(super.addEdge(sourceVertex, targetVertex) == null) {
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
            return true;
        }
        return false;
    }

    private Vertex getOrCreateNode(Vertex node) {
        for(Vertex vertex: vertexSet()) {
            if(vertex.equals(node)) {
                return vertex;
            }
        }
        addVertex(node);
        return node;
    }

    public void addLootTable(EntityType<?> entityType, List<Item> items) {
        Vertex root = getOrCreateNode(new Vertex(entityType));
        for(Item item: items) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
    }

    public void addLootTable(Block block, List<Item> items) {
        Vertex root = getOrCreateNode(new Vertex(block));
        for(Item item: items) {
            Vertex vertex = getOrCreateNode(new Vertex(item));
            addEdge(root, vertex);
            addVertex(vertex);
        }
    }

    private Set<Vertex> getParents(Vertex vertex, Set<Vertex> visited, Set<Vertex> parents) {
        if(visited.contains(vertex)) {
            return parents;
        }
        visited.add(vertex);
        for(Edge edge: incomingEdgesOf(vertex)) {
            Vertex parent = getEdgeSource(edge);
            parents.add(parent);
            getParents(parent, visited, parents);
        }
        return parents;
    }

    public Set<Vertex> getParents(Vertex vertex) {
        return getParents(vertex, new HashSet<>(), new HashSet<>());
    }

    private Set<Vertex> getChildren(Vertex vertex, Set<Vertex> visited, Set<Vertex> children) {
        if(visited.contains(vertex)) {
            return children;
        }
        visited.add(vertex);
        for(Edge edge: outgoingEdgesOf(vertex)) {
            Vertex child = getEdgeTarget(edge);
            children.add(child);
            getChildren(child, visited, children);
        }
        return children;
    }

    public Set<Vertex> getChildren(Vertex vertex) {
        return getChildren(vertex, new HashSet<>(), new HashSet<>());
    }

    public class Vertex implements graph.elements.Vertex {
        private LootTableType type;
        private boolean highlightAsParent = false;
        private boolean highlightAsChild = false;
        private boolean highlightAsTarget = false;

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
            if(type.isItem) return type.getItem();
            return null;
        }

        public Block getBlock() {
            if(type.isBlock) return type.getBlock();
            return null;
        }

        public EntityType<?> getEntityType() {
            if(type.isEntity) return type.getEntityType();
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

        @Override
        public void setSize(Dimension dimension) {
        }

        @Override
        public void setContent(Object o) {
            if(o instanceof Block) {
                type = new LootTableType((Block) o);
            } else if (o instanceof Item) {
                type = new LootTableType((Item) o);
            } else if (o instanceof EntityType) {
                type = new LootTableType((EntityType<?>) o);
            }
        }

        public Text getTooltip() {
            if(type.isBlock) {
                return type.getBlock().getName();
            } else if (type.isItem) {
                return type.getItem().getName();
            } else {
                return type.getEntityType().getName();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Vertex other) {
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

        public Iterable<Vertex> getParents() {
            return LootTableGraph.this.getParents(this);
        }

        public Iterable<Vertex> getChildren() {
            return LootTableGraph.this.getChildren(this);
        }

        public void highlightAsParent() {
            highlightAsParent = true;
        }

        public void unhighlightAsParent() {
            highlightAsParent = false;
        }

        public boolean isHighlightedAsParent() {
            return highlightAsParent;
        }

        public void highlightAsChild() {
            highlightAsChild = true;
        }

        public void unhighlightAsChild() {
            highlightAsChild = false;
        }

        public boolean isHighlightedAsChild() {
            return highlightAsChild;
        }

        public void highlightAsTarget() {
            highlightAsTarget = true;
        }

        public void unhighlightAsTarget() {
            highlightAsTarget = false;
        }

        public boolean isHighlightedAsTarget() {
            return highlightAsTarget;
        }
    }

    public static class Edge extends DefaultEdge implements graph.elements.Edge<Vertex> {
        private Vertex origin;
        private Vertex destination;

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
        public void setOrigin(Vertex vertex) {
            origin = vertex;
        }

        @Override
        public void setDestination(Vertex vertex) {
            destination = vertex;
        }

        @Override
        public int getWeight() {
            return 0;
        }

        @Override
        public void setWeight(int i) {
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
            if(block == null && item != null) {
                block = Block.getBlockFromItem(item);
                if(block.asItem() == Items.AIR) block = null;
            } else if(block != null && item == null) {
                item = block.asItem();
                if(item == Items.AIR) item = null;
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
            if(!isBlock) return null;
            return block;
        }

        public Item getItem() {
            if(!isItem) return null;
            return item;
        }

        public EntityType<?> getEntityType() {
            if(!isEntity) return null;
            return entityType;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LootTableType other) {
                if(isBlock) {
                    return other.isBlock && block.equals(other.block);
                } else if(isItem) {
                    return other.isItem && item.equals(other.item);
                } else if(isEntity) {
                    return other.isEntity && entityType.equals(other.entityType);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            if(isBlock) {
                return Registries.BLOCK.getId(block).toString().hashCode();
            } else if(isItem) {
                return Registries.ITEM.getId(item).toString().hashCode();
            } else if(isEntity) {
                return Registries.ENTITY_TYPE.getId(entityType).toString().hashCode();
            }
            return 0;
        }

        @Override
        public String toString() {
            return "LootTableType{" + (isBlock ? "block=" + block : isItem ? "item=" + item : isEntity ? "entityType=" + entityType : null) + '}';
        }
    }
}
