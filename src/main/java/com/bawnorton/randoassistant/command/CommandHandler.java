package com.bawnorton.randoassistant.command;

import com.bawnorton.randoassistant.networking.Networking;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
                registerDebugCommand(dispatcher);
                registerTriggerAllLootTablesCommand(dispatcher);
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
                    return 0;
        }));
    }
}
