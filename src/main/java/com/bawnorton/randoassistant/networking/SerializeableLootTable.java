package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Triplet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SerializeableLootTable {
    private Triplet<String, String, List<String>> serializedLootTable;

    private final Identifier lootTableId;
    private final Identifier sourceId;
    private final List<Item> items;

    private SerializeableLootTable(Identifier lootTableId, Identifier sourceId, Collection<ItemStack> items) {
        if(lootTableId == null) throw new IllegalArgumentException("Identifier cannot be null");
        this.lootTableId = lootTableId;
        this.sourceId = sourceId;
        this.items = new ArrayList<>();
        for(ItemStack itemStack : items) {
            this.items.add(itemStack.getItem());
        }

        initSerialized();
    }

    private void initSerialized() {
        List<String> itemNames = new ArrayList<>();
        for(Item item : items) {
            itemNames.add(Registries.ITEM.getId(item).toString());
        }
        serializedLootTable = new Triplet<>(lootTableId.toString(), sourceId.toString(), itemNames);
    }

    private static SerializeableLootTable deserialize(Triplet<String, String, List<String>> serialized) {
        String lootTableId = serialized.a();
        String sourceId = serialized.b();
        List<ItemStack> items = new ArrayList<>();
        for(String item : serialized.c()) {
            items.add(new ItemStack(Registries.ITEM.get(new Identifier(item))));
        }
        return new SerializeableLootTable(new Identifier(lootTableId), new Identifier(sourceId), items);
    }

    public static SerializeableLootTable ofBlock(Block block, Collection<ItemStack> items) {
        return new SerializeableLootTable(block.getLootTableId(), Registries.BLOCK.getId(block), items);
    }

    public static SerializeableLootTable ofEntity(EntityType<?> entity, Collection<ItemStack> items) {
        return new SerializeableLootTable(entity.getLootTableId(), Registries.ENTITY_TYPE.getId(entity), items);
    }

    public static SerializeableLootTable ofOther(Identifier id, Collection<ItemStack> items) {
        return new SerializeableLootTable(id, id, items);
    }

    public Identifier getLootTableId() {
        return lootTableId;
    }

    public Identifier getSourceId() {
        return sourceId;
    }

    public List<Item> getItems() {
        return items;
    }

    @SuppressWarnings("unchecked")
    public static SerializeableLootTable fromBytes(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return deserialize((Triplet<String, String, List<String>>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(serializedLootTable);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SerializeableLootTable{");
        sb.append("source=").append(lootTableId);
        sb.append(", items=[");
        for(Item item : items) {
            sb.append(item).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]}");
        return sb.toString();
    }
}
