package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.LootCondition;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SerializeableLootTable implements Serializeable {
    private final Identifier lootTableId;
    private final Identifier sourceId;
    private final List<Item> items;
    private final LootCondition condition;

    private SerializeableLootTable(Identifier lootTableId, Identifier sourceId, List<Item> items, LootCondition condition) {
        if(lootTableId == null) throw new IllegalArgumentException("Identifier cannot be null");
        this.lootTableId = lootTableId;
        this.sourceId = sourceId;
        this.items = new ArrayList<>(items);
        this.condition = condition;
    }

    private SerializeableLootTable(Identifier lootTableId, Identifier sourceId, Collection<ItemStack> stacks, LootCondition condition) {
        this(lootTableId, sourceId, stacks.stream().map(ItemStack::getItem).toList(), condition);
    }

    public static SerializeableLootTable deserialize(PacketByteBuf buf) {
        Identifier lootTableId = buf.readIdentifier();
        Identifier sourceId = buf.readIdentifier();
        LootCondition condition = LootCondition.valueOf(buf.readString());
        List<Item> items = buf.readCollection(ArrayList::new, (byteBuf) -> Registries.ITEM.get(byteBuf.readIdentifier()));
        return new SerializeableLootTable(lootTableId, sourceId, items, condition);
    }

    public PacketByteBuf serialize() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(lootTableId);
        buf.writeIdentifier(sourceId);
        buf.writeString(condition.name());
        buf.writeCollection(items, (byteBuf, item) -> byteBuf.writeIdentifier(Registries.ITEM.getId(item)));
        return buf;
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
