package com.bawnorton.randoassistant.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LootTableMap implements Iterable<Map.Entry<Identifier, List<ItemStack>>> {
    private final Map<String, List<String>> serializedLootTableMap;
    private final Map<Identifier, List<ItemStack>> lootTableMap;


    public LootTableMap(Map<Identifier, List<ItemStack>> lootTableMap) {
        this.lootTableMap = lootTableMap;
        this.serializedLootTableMap = new HashMap<>();
        initSerializedLootTable();
    }

    private void initSerializedLootTable() {
        for (Map.Entry<Identifier, List<ItemStack>> entry : lootTableMap.entrySet()) {
            Identifier lootTableId = entry.getKey();
            serializedLootTableMap.put(lootTableId.toString(), new ArrayList<>());

            List<ItemStack> lootTable = entry.getValue();
            for (ItemStack itemStack : lootTable) {
                Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
                serializedLootTableMap.get(lootTableId.toString()).add(itemId.toString());
            }
        }
    }

    public static LootTableMap fromSerialized(Map<String, List<String>> serializedLootTableMap) {
        Map<Identifier, List<ItemStack>> lootTableMap = new HashMap<>();
        if(serializedLootTableMap == null) return new LootTableMap(lootTableMap);
        for (Map.Entry<String, List<String>> entry : serializedLootTableMap.entrySet()) {
            Identifier lootTableId = new Identifier(entry.getKey());
            List<ItemStack> lootTable = new ArrayList<>();
            for(String type: entry.getValue()) {
                Identifier itemId = new Identifier(type);
                ItemStack itemStack = new ItemStack(Registries.ITEM.get(itemId));
                lootTable.add(itemStack);
            }
            lootTableMap.put(lootTableId, lootTable);
        }
        return new LootTableMap(lootTableMap);
    }

    public Map<Identifier, List<ItemStack>> getLootTableMap() {
        return lootTableMap;
    }

    public Map<String, List<String>> getSerializedLootTableMap() {
        return serializedLootTableMap;
    }

    public void addLootTable(Identifier lootTableId, List<ItemStack> lootTable) {
        if(lootTableMap.containsKey(lootTableId)) {
            List<ItemStack> oldLootTable = lootTableMap.get(lootTableId);
            for (ItemStack itemStack : lootTable) {
                Item item = itemStack.getItem();
                boolean found = false;
                for (ItemStack oldItemStack : oldLootTable) {
                    if(oldItemStack.getItem() == item) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    oldLootTable.add(itemStack);
                }
            }
            lootTableMap.put(lootTableId, oldLootTable);
        } else {
            lootTableMap.put(lootTableId, lootTable);
        }
        serializedLootTableMap.put(lootTableId.toString(), new ArrayList<>());
        for (ItemStack itemStack : lootTable) {
            Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
            serializedLootTableMap.get(lootTableId.toString()).add(itemId.toString());
        }
    }

    public void removeLootTable(Identifier lootTableId) {
        lootTableMap.remove(lootTableId);
        serializedLootTableMap.remove(lootTableId.toString());
    }

    public boolean hasLootTable(Identifier lootTableId) {
        return lootTableMap.containsKey(lootTableId);
    }

    public List<ItemStack> getLootTable(Identifier lootTableId) {
        return lootTableMap.get(lootTableId);
    }

    public void clear() {
        lootTableMap.clear();
        serializedLootTableMap.clear();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<Identifier, List<ItemStack>>> iterator() {
        return lootTableMap.entrySet().iterator();
    }
}
