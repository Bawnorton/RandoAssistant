package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.LootAdvancement;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.SmithingTrimRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Networking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.HANDSHAKE_PACKET, (server, player, handler, buf, responseSender) -> sendHandshakePacket(player));
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.STATS_PACKET, (server, player, handler, buf, responseSender) -> player.getStatHandler().sendStats(player));
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.ADVANCEMENT_UNLOCK_PACKET, (server, player, handler, buf, responseSender) -> LootAdvancement.fromOrdinal(buf.readInt()).grant(player));
    }

    public static void sendSerializeablePacket(ServerPlayerEntity player, Serializeable serializeable) {
        ServerPlayNetworking.send(player, serializeable.getTypePacket(), serializeable.serialize());
    }

    public static void sendClearCachePacket(ServerPlayerEntity serverPlayer) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(serverPlayer, NetworkingConstants.CLEAR_CACHE_PACKET, buf);
    }

    public static void sendDebugPacket(ServerPlayerEntity player, Item item) {
        PacketByteBuf buf = PacketByteBufs.create();
        Block block = Block.getBlockFromItem(item);
        buf.writeItemStack(item.getDefaultStack());
        buf.writeRegistryValue(Registries.CUSTOM_STAT, block.getLootTableId());
        ServerPlayNetworking.send(player, NetworkingConstants.DEBUG_PACKET, buf);
    }

    public static void sendHandshakePacket(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, NetworkingConstants.HANDSHAKE_PACKET, PacketByteBufs.create());
        player.getStatHandler().sendStats(player);
    }

    public static void sendData(@NotNull MinecraftServer server, ServerPlayerEntity player) {
        sendAllLootTables(server, player);
        sendAllRecipes(server, player);
        sendAllInteractions(player);
        sendClearCachePacket(player);
    }

    private static void sendAllLootTables(MinecraftServer server, ServerPlayerEntity player) {
        LootManager lootManager = server.getLootManager();
        LootContextType lootContextType = new LootContextType.Builder().allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL).build();

        for(int i = 0; i < 50; i++) {
            HashSet<Identifier> seen = new HashSet<>();
            Registries.BLOCK.forEach(block -> {
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(player.getServerWorld())
                        .addOptional(LootContextParameters.THIS_ENTITY, player)
                        .luck(100f);

                seen.add(block.getLootTableId());
                LootTable table = lootManager.getLootTable(block.getLootTableId());
                Set<ItemStack> stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofBlock(block, stacks, false));

                ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
                pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                builder.addOptional(LootContextParameters.TOOL, pickaxe);
                stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofBlock(block, stacks, true));
            });

            Registries.ENTITY_TYPE.forEach(entityType -> {
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(player.getServerWorld())
                        .addOptional(LootContextParameters.THIS_ENTITY, player)
                        .luck(100f);

                seen.add(entityType.getLootTableId());
                Entity entity = entityType.create(server.getWorld(World.OVERWORLD));
                if (!(entity instanceof LivingEntity)) return;
                LootTable table = lootManager.getLootTable(entityType.getLootTableId());
                Set<ItemStack> stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofEntity(entityType, stacks));
            });

            lootManager.getIds(LootDataType.LOOT_TABLES).forEach(id -> {
                if (seen.contains(id)) return;
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(player.getServerWorld())
                        .addOptional(LootContextParameters.THIS_ENTITY, player)
                        .luck(100f);

                LootTable table = lootManager.getLootTable(id);
                List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofOther(id, stacks));
            });
        }
    }

    private static void sendAllRecipes(MinecraftServer server, ServerPlayerEntity player) {
        RecipeManager recipeManager = server.getRecipeManager();
        recipeManager.values().forEach(recipe -> {
            if(recipe instanceof SmithingTrimRecipe) return;
            Item output = recipe.getOutput(DynamicRegistryManager.of(Registries.REGISTRIES)).getItem();
            Networking.sendSerializeablePacket(player, SerializeableCrafting.of(recipe, output));
        });
    }

    private static void sendAllInteractions(ServerPlayerEntity player) {
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        OxidizableBlock.OXIDATION_LEVEL_DECREASES.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        AxeItem.STRIPPED_BLOCKS.forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
    }

    public static void sendTakePanoramaPacket(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, NetworkingConstants.TAKE_PANORAMA_PACKET, buf);
    }
}
