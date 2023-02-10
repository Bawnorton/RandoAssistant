package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.util.LootTableMap;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
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
	}

	public static LootTableMap getCurrentLootTables() {
		return LOOT_TABLES.get(currentServer);
	}

	public static void setCurrentLootTables(LootTableMap lootTables) {
		LOOT_TABLES.put(currentServer, lootTables);
	}

	public static void addLootTable(Block block, List<ItemStack> table) {
		RandoAssistant.LOOT_TABLES.get(currentServer).addLootTable(block, table);
	}

	public static void addLootTable(EntityType<?> entityType, List<ItemStack> table) {
		RandoAssistant.LOOT_TABLES.get(currentServer).addLootTable(entityType, table);
	}
}