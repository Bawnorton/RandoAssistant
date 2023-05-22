package com.bawnorton.randoassistant.command;

import com.bawnorton.randoassistant.networking.Networking;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.LootManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
                registerDebugCommand(dispatcher);
                registerTriggerAllLootTablesCommand(dispatcher);
                registerGenerateChestsCommand(dispatcher);
            }
        }));
    }

    private static void registerDebugCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("debug").requires((source) -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    assert player != null;
                    Item item = player.getMainHandStack().getItem();
                    Networking.sendDebugPacket(player, item);
                    return 0;
        }));
    }

    private static void registerTriggerAllLootTablesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("triggerAllLootTables").requires((source) -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    assert player != null;
                    ServerWorld world = player.getWorld();
                    BlockPos up = player.getBlockPos().add(0, 20, 0);
                    Registries.BLOCK.forEach((block -> {
                        world.setBlockState(up, block.getDefaultState(), 0);
                        world.breakBlock(up, true, player);
                    }));
                    Registries.ENTITY_TYPE.forEach((entityType -> {
                        Entity entity = entityType.create(world);
                        if(!(entity instanceof LivingEntity)) return;
                        entity.updatePosition(player.getX(), player.getY() + 1, player.getZ());
                        world.spawnEntity(entity);
                        entity.damage(world.getDamageSources().playerAttack(player), Float.MAX_VALUE);
                    }));
                    /* for(VillagerProfession profession: Set.of(
                            VillagerProfession.WEAPONSMITH,
                            VillagerProfession.TOOLSMITH,
                            VillagerProfession.BUTCHER,
                            VillagerProfession.LEATHERWORKER,
                            VillagerProfession.CARTOGRAPHER,
                            VillagerProfession.CLERIC,
                            VillagerProfession.FARMER,
                            VillagerProfession.FISHERMAN,
                            VillagerProfession.FLETCHER,
                            VillagerProfession.LIBRARIAN,
                            VillagerProfession.MASON,
                            VillagerProfession.SHEPHERD
                    )) {
                        VillagerEntity villager = EntityType.VILLAGER.create(world);
                        assert villager != null;
                        villager.updatePosition(player.getX() + 1, player.getY() + 1, player.getZ() + 1);
                        villager.setVillagerData(villager.getVillagerData().withProfession(profession));
                        world.spawnEntity(villager);
                    }
                    StatusEffectInstance effectInstance = new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, -1, 255, false, false);
                    player.addStatusEffect(effectInstance);
                     */
                    return 0;
        }));
    }

    private static void registerGenerateChestsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("genChests").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    assert player != null;
                    ServerWorld world = player.getWorld();
                    BlockPos up = player.getBlockPos().add(0, 20, 0);
                    LootManager manager = world.getServer().getLootManager();
                    AtomicInteger x = new AtomicInteger();
                    manager.getTableIds().forEach(id -> {
                        if(id.getPath().contains("chests")) {
                            world.setBlockState(up.add(x.get(), 0, 20), Blocks.CHEST.getDefaultState(), 0);
                            ChestBlockEntity chest = (ChestBlockEntity) world.getBlockEntity(up.add(x.get(), 0, 20));
                            assert chest != null;
                            chest.setLootTable(id, world.getRandom().nextLong());
                            x.getAndIncrement();
                        }
                    });
                    return 0;
                }));
    }
}
