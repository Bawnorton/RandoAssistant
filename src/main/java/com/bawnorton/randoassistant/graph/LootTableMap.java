package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.WallBlockLookup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class LootTableMap {
    private final Map<String, List<String>> serializedLootTableMap;
    private final LootTableGraph lootTableGraph;

    private final Map<Block, List<Item>> blockLootTables;
    private final Map<EntityType<?>, List<Item>> entityLootTables;
    private final Map<Identifier, List<Item>> otherLootTables;
    private final HashSet<Item> knownItems;


    private LootTableMap(Map<Block, List<Item>> blockLootTables, Map<EntityType<?>, List<Item>> entityLootTables, Map<Identifier, List<Item>> otherLootTables, Set<Item> knownItems) {
        this.blockLootTables = blockLootTables;
        this.entityLootTables = entityLootTables;
        this.otherLootTables = otherLootTables;
        this.knownItems = new HashSet<>(knownItems);
        this.serializedLootTableMap = new HashMap<>();
        this.lootTableGraph = new LootTableGraph();

        lootTableGraph.getExecutor().disableDrawTask();
        blockLootTables.forEach(lootTableGraph::addLootTable);
        entityLootTables.forEach(lootTableGraph::addLootTable);
        otherLootTables.forEach(lootTableGraph::addLootTable);
        lootTableGraph.getExecutor().enableDrawTask();

        initSerializedLootTable();
    }

    public LootTableMap() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>());
    }

    public static LootTableMap fromSerialized(Map<String, List<String>> serializedLootTableMap) {
        Map<Block, List<Item>> blockLootTables = new HashMap<>();
        Map<EntityType<?>, List<Item>> entityLootTables = new HashMap<>();
        Map<Identifier, List<Item>> otherLootTables = new HashMap<>();
        Set<Item> knownItems = new HashSet<>();

        if (serializedLootTableMap == null) return new LootTableMap();
        for (Map.Entry<String, List<String>> entry : serializedLootTableMap.entrySet()) {
            Identifier identifier = new Identifier(entry.getKey());
            List<Item> lootTable = new ArrayList<>();
            for (String type : entry.getValue()) {
                Item item = Registries.ITEM.get(new Identifier(type));
                lootTable.add(item);
                knownItems.add(item);
            }

            if (Registries.BLOCK.containsId(identifier)) {
                blockLootTables.put(Registries.BLOCK.get(identifier), lootTable);
            } else if (Registries.ENTITY_TYPE.containsId(identifier)) {
                entityLootTables.put(Registries.ENTITY_TYPE.get(identifier), lootTable);
            } else {
                otherLootTables.put(identifier, lootTable);
            }
        }
        return new LootTableMap(blockLootTables, entityLootTables, otherLootTables, knownItems);
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
        for (Map.Entry<Identifier, List<Item>> entry : otherLootTables.entrySet()) {
            Identifier identifier = entry.getKey();
            serializedLootTableMap.put(identifier.toString(), new ArrayList<>());

            List<Item> lootTable = entry.getValue();
            for (Item loot : lootTable) {
                serializedLootTableMap.get(identifier.toString()).add(Registries.ITEM.getId(loot).toString());
            }
        }
    }

    private void processLootTable(List<ItemStack> in, List<Item> out) {
        for (ItemStack itemStack : in) {
            Item item = itemStack.getItem();
            boolean found = false;
            for (Item oldItemStack : out) {
                if (oldItemStack == item) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.add(itemStack.getItem());
            }
        }
    }

    private boolean lootTableChanged(List<ItemStack> in, List<Item> out) {
        for (ItemStack itemStack : in) {
            Item item = itemStack.getItem();
            boolean found = false;
            for (Item oldItemStack : out) {
                if (oldItemStack == item) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> filterAirAndDuplicates(List<ItemStack> lootTable) {
        List<ItemStack> filteredLootTable = new ArrayList<>();
        for (ItemStack itemStack : lootTable) {
            if (itemStack.getItem() == Blocks.AIR.asItem()) continue;
            boolean found = false;
            for (ItemStack filteredItemStack : filteredLootTable) {
                if (filteredItemStack.getItem() == itemStack.getItem()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                filteredLootTable.add(itemStack);
            }
        }
        return filteredLootTable;
    }

    public void addLootTable(Block block, List<ItemStack> items) {
        block = WallBlockLookup.getBlock(block);
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (blockLootTables.containsKey(block)) {
            lootTable = blockLootTables.get(block);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = new ArrayList<>();
            for (ItemStack itemStack : items) {
                lootTable.add(itemStack.getItem());
            }
            changed = true;
        }
        if (changed) {
            blockLootTables.put(block, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(block, lootTable);
            serializedLootTableMap.put(Registries.BLOCK.getId(block).toString(), new ArrayList<>());
            for (Item item : blockLootTables.get(block)) {
                serializedLootTableMap.get(Registries.BLOCK.getId(block).toString()).add(Registries.ITEM.getId(item).toString());
            }
        }
    }

    public void addLootTable(EntityType<?> entityType, List<ItemStack> items) {
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (entityLootTables.containsKey(entityType)) {
            lootTable = entityLootTables.get(entityType);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = new ArrayList<>();
            for (ItemStack itemStack : items) {
                lootTable.add(itemStack.getItem());
            }
            changed = true;
        }
        if (changed) {
            entityLootTables.put(entityType, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(entityType, lootTable);
            serializedLootTableMap.put(Registries.ENTITY_TYPE.getId(entityType).toString(), new ArrayList<>());
            for (Item item : entityLootTables.get(entityType)) {
                serializedLootTableMap.get(Registries.ENTITY_TYPE.getId(entityType).toString()).add(Registries.ITEM.getId(item).toString());
            }
        }
    }

    public void addChestLootTable(Identifier lootTableId, List<ItemStack> items) {
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (otherLootTables.containsKey(lootTableId)) {
            lootTable = otherLootTables.get(lootTableId);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = new ArrayList<>();
            for (ItemStack itemStack : items) {
                lootTable.add(itemStack.getItem());
            }
            changed = true;
        }
        if (changed) {
            otherLootTables.put(lootTableId, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(lootTableId, lootTable);
            serializedLootTableMap.put(lootTableId.toString(), new ArrayList<>());
            for (Item item : otherLootTables.get(lootTableId)) {
                serializedLootTableMap.get(lootTableId.toString()).add(Registries.ITEM.getId(item).toString());
            }
        }
    }

    public boolean isKnownItem(Item item) {
        return knownItems.contains(item);
    }

    public Map<String, List<String>> getSerializedLootTableMap() {
        return serializedLootTableMap;
    }

    public LootTableGraph getGraph() {
        return lootTableGraph;
    }
}
