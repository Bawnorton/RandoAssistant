package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.LootCondition;
import com.bawnorton.randoassistant.util.tuples.Quadruplet;
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

public class SerializeableLootTable implements Serializeable {
    private Quadruplet<String, String, List<String>, String> serializedLootTable;

    private Identifier lootTableId;
    private Identifier sourceId;
    private List<Item> items;
    private LootCondition condition;

    private SerializeableLootTable(Identifier lootTableId, Identifier sourceId, Collection<ItemStack> items, LootCondition condition) {
        if(lootTableId == null) throw new IllegalArgumentException("Identifier cannot be null");
        this.lootTableId = lootTableId;
        this.sourceId = sourceId;
        this.items = new ArrayList<>();
        for(ItemStack itemStack : items) {
            this.items.add(itemStack.getItem());
        }
        this.condition = condition;

        initSerialized();
    }

    public SerializeableLootTable(byte[] bytes) {
        populateData(bytes);
    }

    private void initSerialized() {
        List<String> itemNames = new ArrayList<>();
        for(Item item : items) {
            itemNames.add(Registries.ITEM.getId(item).toString());
        }
        serializedLootTable = new Quadruplet<>(lootTableId.toString(), sourceId.toString(), itemNames, condition.name());
    }

    private void deserialize(Quadruplet<String, String, List<String>, String> serialized) {
        this.lootTableId = new Identifier(serialized.a());
        this.sourceId = new Identifier(serialized.b());
        this.condition = LootCondition.valueOf(serialized.d());
        this.items = serialized.c().stream().map(itemName -> Registries.ITEM.get(new Identifier(itemName))).toList();
    }

    public static SerializeableLootTable ofBlock(Block block, Collection<ItemStack> items, boolean silkTouch) {
        return new SerializeableLootTable(block.getLootTableId(), Registries.BLOCK.getId(block), items, silkTouch ? LootCondition.SILK_TOUCH : LootCondition.NONE);
    }

    public static SerializeableLootTable ofEntity(EntityType<?> entity, Collection<ItemStack> items) {
        return new SerializeableLootTable(entity.getLootTableId(), Registries.ENTITY_TYPE.getId(entity), items, LootCondition.NONE);
    }

    public static SerializeableLootTable ofOther(Identifier id, Collection<ItemStack> items) {
        return new SerializeableLootTable(id, id, items, LootCondition.NONE);
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

    public LootCondition getCondition() {
        return condition;
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

    @SuppressWarnings("unchecked")
    public void populateData(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            deserialize((Quadruplet<String, String, List<String>, String>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Identifier getTypePacket() {
        return NetworkingConstants.LOOT_TABLE_PACKET;
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
        sb.append("], condition=").append(condition);
        sb.append('}');
        return sb.toString();
    }
}
