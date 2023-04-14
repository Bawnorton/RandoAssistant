package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.config.ServerConfigManager;
import com.bawnorton.randoassistant.entity.Penguin;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.registry.Registrar;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.*;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Map<ServerWorld, Penguin> THE_PENGUIN = new HashMap<>();

    public static final Map<CandleCakeBlock, CandleBlock> CANDLE_CAKE_MAP = new HashMap<>();

    public static void addAllLootTables(PlayerEntity clientPlayer) {
        ServerPlayerEntity serverPlayer = Networking.server.getPlayerManager().getPlayer(clientPlayer.getUuid());
        LootManager lootManager = Networking.server.getLootManager();
        Networking.server.execute(() -> {
            LootContextType lootContextType = new LootContextType.Builder().allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL).build();
            for (int i = 0; i < 200; i++) { // random drop chances are fun... will probably get all possible drops
                Registries.BLOCK.forEach(block -> {
                    LootTable table = lootManager.getTable(block.getLootTableId());
                    LootContext.Builder builder = new LootContext.Builder( Networking.server.getWorld(World.OVERWORLD));
                    builder.optionalParameter(LootContextParameters.THIS_ENTITY, clientPlayer);
                    List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                    Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofBlock(block, stacks));
                    ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
                    pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                    builder.optionalParameter(LootContextParameters.TOOL, pickaxe);
                    stacks = table.generateLoot(builder.build(lootContextType));
                    Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofBlock(block, stacks));
                });
                Registries.ENTITY_TYPE.forEach(entityType -> {
                    LootTable table = lootManager.getTable(entityType.getLootTableId());
                    LootContext.Builder builder = new LootContext.Builder( Networking.server.getWorld(World.OVERWORLD));
                    builder.optionalParameter(LootContextParameters.THIS_ENTITY, clientPlayer);
                    List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                    Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofEntity(entityType, stacks));
                    ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
                    sword.addEnchantment(Enchantments.FIRE_ASPECT, 1);
                    builder.optionalParameter(LootContextParameters.TOOL, sword);
                    stacks = table.generateLoot(builder.build(lootContextType));
                    Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofEntity(entityType, stacks));
                });
            }
            Networking.sendUpdateDrawingPacket(serverPlayer);
            clientPlayer.sendMessage(Text.of("§b[RandoAssistant]: §aAdded all loot tables!"), false);
        });
    }

    @Override
    public void onInitialize() {
        LOGGER.info("RandoAssistant Initialised");
        Registrar.init();
        CommandHandler.init();
        ServerConfigManager.loadConfig();

        FabricDefaultAttributeRegistry.register(Registrar.PENGUIN, Penguin.createAttributes());

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if(entity instanceof Penguin penguin) {
                THE_PENGUIN.put(world, penguin);
                List<ServerPlayerEntity> players = world.getPlayers();
                if(players.size() == 0) return;

                boolean isCaptainPlayer = false;
                ServerPlayerEntity captain = null;
                for(ServerPlayerEntity player : players) {
                    if(player.getUuid().equals(UUID.fromString("5f820c39-5883-4392-b174-3125ac05e38c"))) {
                        isCaptainPlayer = true;
                        captain = player;
                        break;
                    }
                }
                if(!isCaptainPlayer && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    RandoAssistant.LOGGER.warn("Penguins not enabled unless CaptainSparklez is playing!");
                    return;
                }

                penguin.setCustomName(Text.of("Lil' Donk"));
                if(captain == null && FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    if(penguin.getOwner() == null) {
                        penguin.setOwner(players.get(0));
                    }
                } else {
                    penguin.setOwner(captain);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(handler.player.getUuid().equals(UUID.fromString("5f820c39-5883-4392-b174-3125ac05e38c")) || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if(handler.player.world instanceof ServerWorld serverWorld) {
                    Penguin penguin = new Penguin(Registrar.PENGUIN, serverWorld);
                    penguin.setPos(handler.player.getX(), handler.player.getY(), handler.player.getZ());
                    serverWorld.spawnEntity(penguin);
                    penguin.setCustomName(Text.of("Lil' Donk"));
                    penguin.setOwner(handler.player);
                    THE_PENGUIN.put(serverWorld, penguin);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if(handler.player.getUuid().equals(UUID.fromString("5f820c39-5883-4392-b174-3125ac05e38c")) || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if(handler.player.world instanceof ServerWorld serverWorld) {
                    THE_PENGUIN.remove(serverWorld);
                }
            }
        });
    }
}