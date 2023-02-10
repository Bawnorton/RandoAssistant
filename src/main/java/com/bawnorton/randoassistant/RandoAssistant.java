package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.util.LootTableMap;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandoAssistant implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("randoassistant");
	public static MinecraftServer currentServer;

	private static final Map<MinecraftServer, LootTableMap> LOOT_TABLES = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("RandoAssistant Initialised");

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient) return;
			Identifier lootTableId = state.getBlock().getLootTableId();
			if (lootTableId == null) return;
			addLootTable(state.getBlock().getLootTableId());
		});
	}

	public static LootTableMap getCurrentLootTables() {
		return LOOT_TABLES.get(currentServer);
	}

	public static void setCurrentLootTables(LootTableMap lootTables) {
		LOOT_TABLES.put(currentServer, lootTables);
	}

	public static void addLootTable(Identifier lootTableId) {
		LootTable lootTable = currentServer.getLootManager().getTable(lootTableId);
		LootContext lootContext = new LootContext.Builder(currentServer.getWorld(World.OVERWORLD)).build(LootContextType.create().build());
		List<ItemStack> drops = lootTable.generateLoot(lootContext);
		addLootTable(lootTableId, drops);
	}

	public static void addLootTable(Identifier lootTableId, List<ItemStack> table) {
		RandoAssistant.LOOT_TABLES.get(currentServer).addLootTable(lootTableId, table);
	}
}