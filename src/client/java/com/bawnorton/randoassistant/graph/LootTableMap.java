package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.util.WallBlockLookup;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class LootTableMap {
    private static final Codec<Map<String, List<String>>> codec = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING));

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
        this.lootTableGraph = new LootTableGraph();

        lootTableGraph.getExecutor().disableDrawTask();
        blockLootTables.forEach(lootTableGraph::addLootTable);
        entityLootTables.forEach(lootTableGraph::addLootTable);
        otherLootTables.forEach(lootTableGraph::addLootTable);
        lootTableGraph.getExecutor().enableDrawTask();
    }

    public LootTableMap() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>());
    }

    public JsonElement serialize() {
        Map<String, List<String>> serializedLootTableMap = new HashMap<>();

        for (Map.Entry<Block, List<Item>> entry : blockLootTables.entrySet()) {
            serializedLootTableMap.put(Registries.BLOCK.getId(entry.getKey()).toString(), new ArrayList<>());
            for (Item item : entry.getValue()) {
                serializedLootTableMap.get(Registries.BLOCK.getId(entry.getKey()).toString()).add(Registries.ITEM.getId(item).toString());
            }
        }

        for (Map.Entry<EntityType<?>, List<Item>> entry : entityLootTables.entrySet()) {
            serializedLootTableMap.put(Registries.ENTITY_TYPE.getId(entry.getKey()).toString(), new ArrayList<>());
            for (Item item : entry.getValue()) {
                serializedLootTableMap.get(Registries.ENTITY_TYPE.getId(entry.getKey()).toString()).add(Registries.ITEM.getId(item).toString());
            }
        }

        for (Map.Entry<Identifier, List<Item>> entry : otherLootTables.entrySet()) {
            serializedLootTableMap.put(entry.getKey().toString(), new ArrayList<>());
            for (Item item : entry.getValue()) {
                serializedLootTableMap.get(entry.getKey().toString()).add(Registries.ITEM.getId(item).toString());
            }
        }

        DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, serializedLootTableMap);
        if(result.error().isPresent()) {
            RandoAssistant.LOGGER.error(result.error().get().message());
        } else if(result.result().isPresent()) {
            return result.result().get();
        }
        RandoAssistant.LOGGER.error("Failed to serialize loot table map");
        RandoAssistant.LOGGER.error("Result: " + result);
        return null;
    }

    public static LootTableMap deserialize(JsonElement json) {
        if(json == null) return new LootTableMap();
        DataResult<Map<String, List<String>>> result = codec.parse(JsonOps.INSTANCE, json);
        if(result.error().isPresent()) {
            RandoAssistant.LOGGER.error(result.error().get().message());
        } else if(result.result().isPresent()) {
            Map<String, List<String>> serializedLootTableMap = result.result().get();
            Map<Block, List<Item>> blockLootTables = new HashMap<>();
            Map<EntityType<?>, List<Item>> entityLootTables = new HashMap<>();
            Map<Identifier, List<Item>> otherLootTables = new HashMap<>();
            Set<Item> knownItems = new HashSet<>();

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
        RandoAssistant.LOGGER.error("Failed to deserialize loot table map");
        RandoAssistant.LOGGER.error("Result: " + result);
        return null;
    }

    private void processLootTable(List<Item> in, List<Item> out) {
        for (Item item : in) {
            boolean found = false;
            for (Item oldItemStack : out) {
                if (oldItemStack == item) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.add(item);
            }
        }
    }

    private boolean lootTableChanged(List<Item> in, List<Item> out) {
        for (Item item : in) {
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

    private List<Item> filterAirAndDuplicates(List<Item> lootTable) {
        List<Item> filteredLootTable = new ArrayList<>();
        for (Item item : lootTable) {
            if (item == Blocks.AIR.asItem()) continue;
            boolean found = false;
            for (Item filteredItemStack : filteredLootTable) {
                if (filteredItemStack == item) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                filteredLootTable.add(item);
            }
        }
        return filteredLootTable;
    }

    private void addLootTable(Block block, List<Item> items) {
        block = WallBlockLookup.getBlock(block);
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (blockLootTables.containsKey(block)) {
            lootTable = blockLootTables.get(block);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = items;
            changed = true;
        }
        if (changed) {
            blockLootTables.put(block, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(block, lootTable);
        }
    }

    private void addLootTable(EntityType<?> entityType, List<Item> items) {
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (entityLootTables.containsKey(entityType)) {
            lootTable = entityLootTables.get(entityType);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = items;
            changed = true;
        }
        if (changed) {
            entityLootTables.put(entityType, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(entityType, lootTable);
        }
    }

    private void addChestLootTable(Identifier lootTableId, List<Item> items) {
        items = filterAirAndDuplicates(items);
        List<Item> lootTable;
        boolean changed;
        if (otherLootTables.containsKey(lootTableId)) {
            lootTable = otherLootTables.get(lootTableId);
            changed = lootTableChanged(items, lootTable);
            processLootTable(items, lootTable);
        } else {
            lootTable = items;
            changed = true;
        }
        if (changed) {
            otherLootTables.put(lootTableId, lootTable);
            knownItems.addAll(lootTable);
            lootTableGraph.addLootTable(lootTableId, lootTable);
        }
    }

    public void addLootTable(SerializeableLootTable lootTable) {
        if (lootTable.isBlock()) {
            addLootTable(lootTable.getBlock(), lootTable.getItems());
        } else if (lootTable.isEntity()) {
            addLootTable(lootTable.getEntity(), lootTable.getItems());
        } else if (lootTable.isOther()) {
            addChestLootTable(lootTable.getIdentifier(), lootTable.getItems());
        }
    }

    public boolean isKnownItem(Item item) {
        return knownItems.contains(item);
    }

    public LootTableGraph getGraph() {
        return lootTableGraph;
    }

    @Override
    public String toString() {
        return "LootTableMap{" +
                "blockLootTables=" + blockLootTables +
                ", entityLootTables=" + entityLootTables +
                ", otherLootTables=" + otherLootTables +
                '}';
    }
}
