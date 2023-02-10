package com.bawnorton.randoassistant.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableMap {
    private final Map<String, List<String>> serializedLootTableMap;

    private final Map<Block, List<Item>> blockLootTables;
    private final Map<EntityType<?>, List<Item>> entityLootTables;

    public LootTableMap(Map<Block, List<Item>> blockLootTables, Map<EntityType<?>, List<Item>> entityLootTables) {
        this.blockLootTables = blockLootTables;
        this.entityLootTables = entityLootTables;
        this.serializedLootTableMap = new HashMap<>();
        initSerializedLootTable();
    }

    private void initSerializedLootTable() {
        for (Map.Entry<Block, List<Item>> entry : blockLootTables.entrySet()) {
            Block block = entry.getKey();
            serializedLootTableMap.put(Registries.BLOCK.getId(block).toString(), new ArrayList<>());

            List<Item> lootTable = entry.getValue();
            for (Item loot : lootTable) {
                serializedLootTableMap.get(Registries.BLOCK.getId(block).toString()).add(Registries.ITEM.getId(loot).toString());
            }
        }
        for (Map.Entry<EntityType<?>, List<Item>> entry : entityLootTables.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            serializedLootTableMap.put(Registries.ENTITY_TYPE.getId(entityType).toString(), new ArrayList<>());

            List<Item> lootTable = entry.getValue();
            for (Item loot : lootTable) {
                serializedLootTableMap.get(Registries.ENTITY_TYPE.getId(entityType).toString()).add(Registries.ITEM.getId(loot).toString());
            }
        }
    }

    public static LootTableMap fromSerialized(Map<String, List<String>> serializedLootTableMap) {
        Map<Block, List<Item>> blockLootTables = new HashMap<>();
        Map<EntityType<?>, List<Item>> entityLootTables = new HashMap<>();

        if(serializedLootTableMap == null) return new LootTableMap(blockLootTables, entityLootTables);
        for (Map.Entry<String, List<String>> entry : serializedLootTableMap.entrySet()) {
            Identifier identifier = new Identifier(entry.getKey());
            List<Item> lootTable = new ArrayList<>();
            for(String type: entry.getValue()) {
                lootTable.add(Registries.ITEM.get(new Identifier(type)));
            }
            if(Registries.BLOCK.get(identifier) == Blocks.AIR) {
                entityLootTables.put(Registries.ENTITY_TYPE.get(identifier), lootTable);
            } else {
                blockLootTables.put(Registries.BLOCK.get(identifier), lootTable);
            }
        }
        return new LootTableMap(blockLootTables, entityLootTables);
    }


    public Map<String, List<String>> getSerializedLootTableMap() {
        return serializedLootTableMap;
    }

    public void addLootTable(Block block, List<ItemStack> lootTable) {
        List<Item> oldLootTable;
        if(blockLootTables.containsKey(block)) {
            oldLootTable = blockLootTables.get(block);
            for (ItemStack itemStack : lootTable) {
                Item item = itemStack.getItem();
                boolean found = false;
                for (Item oldItemStack : oldLootTable) {
                    if(oldItemStack == item) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    oldLootTable.add(itemStack.getItem());
                }
            }
        } else {
            oldLootTable = new ArrayList<>();
            for (ItemStack itemStack : lootTable) {
                oldLootTable.add(itemStack.getItem());
            }
        }
        blockLootTables.put(block, oldLootTable);
        serializedLootTableMap.put(Registries.BLOCK.getId(block).toString(), new ArrayList<>());
        for (Item item : blockLootTables.get(block)) {
            serializedLootTableMap.get(Registries.BLOCK.getId(block).toString()).add(Registries.ITEM.getId(item).toString());
        }
    }

    public void addLootTable(EntityType<?> entityType, List<ItemStack> lootTable) {
        List<Item> oldLootTable;
        if(entityLootTables.containsKey(entityType)) {
            oldLootTable = entityLootTables.get(entityType);
            for (ItemStack itemStack : lootTable) {
                Item item = itemStack.getItem();
                boolean found = false;
                for (Item oldItemStack : oldLootTable) {
                    if(oldItemStack == item) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    oldLootTable.add(itemStack.getItem());
                }
            }
        } else {
            oldLootTable = new ArrayList<>();
            for (ItemStack itemStack : lootTable) {
                oldLootTable.add(itemStack.getItem());
            }
        }
        entityLootTables.put(entityType, oldLootTable);
        serializedLootTableMap.put(Registries.ENTITY_TYPE.getId(entityType).toString(), new ArrayList<>());
        for (Item item : entityLootTables.get(entityType)) {
            serializedLootTableMap.get(Registries.ENTITY_TYPE.getId(entityType).toString()).add(Registries.ITEM.getId(item).toString());
        }
    }

    public LootTableGraph toGraph() {
        LootTableGraph graph = new LootTableGraph();
        for (Map.Entry<Block, List<Item>> entry : blockLootTables.entrySet()) {
            Block block = entry.getKey();
            graph.addLootTable(block, entry.getValue());
        }
        for (Map.Entry<EntityType<?>, List<Item>> entry : entityLootTables.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            graph.addLootTable(entityType, entry.getValue());
        }
        return graph;
    }
}
