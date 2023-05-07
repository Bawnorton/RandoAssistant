package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializeableLootTable {
    private Pair<String, List<String>> serializedLootTable;

    private final Identifier identifier;
    private final List<Item> items;

    private boolean isBlock;
    private boolean isItem;
    private boolean isEntity;
    private boolean isOther;

    private SerializeableLootTable(Identifier id, List<ItemStack> items) {
        if(id == null) throw new IllegalArgumentException("Identifier cannot be null");
        this.identifier = id;
        this.items = new ArrayList<>();
        for(ItemStack itemStack : items) {
            this.items.add(itemStack.getItem());
        }
        this.isBlock = Registries.BLOCK.containsId(identifier);
        if(isBlock) {
            Item item = Registries.BLOCK.get(identifier).asItem();
            isItem = item != null && item != Items.AIR;
        }
        if(!isItem) this.isItem = Registries.ITEM.containsId(identifier);
        if(isItem && !isBlock) {
            Block block = Block.getBlockFromItem(Registries.ITEM.get(identifier));
            isBlock = block != null && block != Blocks.AIR;
        }
        this.isEntity = Registries.ENTITY_TYPE.containsId(identifier);
        this.isOther = !isBlock && !isItem && !isEntity;

        initSerialized();
    }

    private void initSerialized() {
        List<String> itemNames = new ArrayList<>();
        for(Item item : items) {
            itemNames.add(Registries.ITEM.getId(item).toString());
        }
        serializedLootTable = new Pair<>(identifier.toString(), itemNames);
    }

    private static SerializeableLootTable deserialize(Pair<String, List<String>> serialized) {
        String id = serialized.a();
        List<ItemStack> items = new ArrayList<>();
        for(String item : serialized.b()) {
            items.add(new ItemStack(Registries.ITEM.get(new Identifier(item))));
        }
        return new SerializeableLootTable(new Identifier(id), items);
    }

    public static SerializeableLootTable ofBlock(Block block, List<ItemStack> items) {
        return new SerializeableLootTable(Registries.BLOCK.getId(block), items);
    }

    public static SerializeableLootTable ofEntity(EntityType<?> entity, List<ItemStack> items) {
        return new SerializeableLootTable(Registries.ENTITY_TYPE.getId(entity), items);
    }

    public static SerializeableLootTable ofOther(Identifier id, List<ItemStack> items) {
        return new SerializeableLootTable(id, items);
    }

    public boolean isBlock() {
        return isBlock;
    }

    public boolean isItem() {
        return isItem;
    }

    public boolean isEntity() {
        return isEntity;
    }

    public boolean isOther() {
        return isOther;
    }

    public Block getBlock() {
        if(!isBlock) return null;
        return Registries.BLOCK.get(identifier);
    }

    public Item getItem() {
        if(!isItem) return null;
        return Registries.ITEM.get(identifier);
    }

    public EntityType<?> getEntity() {
        if(!isEntity) return null;
        return Registries.ENTITY_TYPE.get(identifier);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<Item> getItems() {
        return items;
    }

    public static SerializeableLootTable fromBytes(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return deserialize((Pair<String, List<String>>) ois.readObject());
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
        sb.append("source=").append(identifier);
        sb.append(", items=[");
        for(Item item : items) {
            sb.append(item).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]}");
        return sb.toString();
    }
}
