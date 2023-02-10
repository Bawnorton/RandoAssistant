package com.bawnorton.randoassistant.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.awt.*;
import java.util.List;

// Directed acyclic graph of loot tables
public class LootTableGraph extends SimpleDirectedGraph<LootTableGraph.Vertex, LootTableGraph.Edge> {
    public LootTableGraph() {
        super(Edge.class);
    }

    private void updateTree(Vertex newNode) {
        for(Vertex node: vertexSet()) {
            if(node.isBlock() && node.getBlockItem().equals(newNode.getLoot())) {
                addEdge(newNode, node);
            }
        }
    }

    public void addLootTable(Block block, List<Item> items) {
        for(Item item: items) {
            Vertex vertex = new Vertex(block, item);
            addVertex(vertex);
            updateTree(vertex);
        }
    }

    public void addLootTable(EntityType<?> entityType, List<Item> items) {
        for(Item item: items) {
            Vertex vertex = new Vertex(entityType, item);
            addVertex(vertex);
            updateTree(vertex);
        }
    }

    public static class Vertex implements graph.elements.Vertex {
        private final LootTableType type;
        private final Item loot;

        public Vertex(Block block, Item loot) {
            this.type = new LootTableType(block);
            this.loot = loot;
        }

        public Vertex(EntityType<?> entityType, Item loot) {
            this.type = new LootTableType(entityType);
            this.loot = loot;
        }

        public boolean isBlock() {
            return type.isBlock;
        }

        public Item getBlockItem() {
            return type.getBlockItem();
        }

        public EntityType<?> getEntityType() {
            return type.getEntityType();
        }

        public Item getLoot() {
            return loot;
        }

        @Override
        public Dimension getSize() {
            return new Dimension(5, 5);
        }

        @Override
        public Object getContent() {
            return null;
        }

        @Override
        public void setSize(Dimension dimension) {
        }

        @Override
        public void setContent(Object o) {
        }
    }

    public static class Edge extends DefaultEdge implements graph.elements.Edge<Vertex> {
        private final Vertex origin;
        private final Vertex destination;

        public Edge(Vertex origin, Vertex destination) {
            this.origin = origin;
            this.destination = destination;
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
        }

        @Override
        public void setDestination(Vertex vertex) {
        }

        @Override
        public int getWeight() {
            return 0;
        }

        @Override
        public void setWeight(int i) {
        }
    }

    private static class LootTableType {
        private final Item blockItem;
        private final EntityType<?> entityType;
        private final boolean isBlock;

        public LootTableType(@Nullable Block block, @Nullable EntityType<?> entityType) {
            this.isBlock = block != null;
            this.blockItem = isBlock ? block.asItem() : null;
            this.entityType = entityType;
        }

        public LootTableType(Block block) {
            this(block, null);
        }

        public LootTableType(EntityType<?> entityType) {
            this(null, entityType);
        }

        public Item getBlockItem() {
            if(!isBlock) return null;
            return blockItem;
        }

        public EntityType<?> getEntityType() {
            if(isBlock) return null;
            return entityType;
        }
    }
}
