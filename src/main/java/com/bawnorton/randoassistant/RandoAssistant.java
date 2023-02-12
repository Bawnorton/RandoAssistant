package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.util.LootTableMap;
import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Block;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
	public static final Map<CandleCakeBlock, CandleBlock> CANDLE_CAKE_MAP = new HashMap<>();
	public static boolean graphChanged = true;

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
		graphChanged = true;
	}

	public static void addLootTable(EntityType<?> entityType, List<ItemStack> table) {
		RandoAssistant.LOOT_TABLES.get(currentServer).addLootTable(entityType, table);
		graphChanged = true;
	}

	public static void addAllLootTables(PlayerEntity player) {
		LootManager lootManager = currentServer.getLootManager();
		player.sendMessage(Text.of("§b[RandoAssistant]: §cAdding all loot tables. World may lag momentarily."), false);
		LootContextType lootContextType = new LootContextType.Builder().allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL).build();
		for(int i = 0; i < 200; i++) { // random drop chances are fun... will probably get all possible drops
			Registries.BLOCK.forEach(block -> {
				LootTable table = lootManager.getTable(block.getLootTableId());
				LootContext.Builder builder = new LootContext.Builder(currentServer.getWorld(World.OVERWORLD));
				builder.optionalParameter(LootContextParameters.THIS_ENTITY, player);
				List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
				RandoAssistant.addLootTable(block, stacks);
				ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
				pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
				builder.optionalParameter(LootContextParameters.TOOL, pickaxe);
				stacks = table.generateLoot(builder.build(lootContextType));
				RandoAssistant.addLootTable(block, stacks);
			});
			Registries.ENTITY_TYPE.forEach(entityType -> {
				LootTable table = lootManager.getTable(entityType.getLootTableId());
				LootContext.Builder builder = new LootContext.Builder(currentServer.getWorld(World.OVERWORLD));
				builder.optionalParameter(LootContextParameters.THIS_ENTITY, player);
				List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
				RandoAssistant.addLootTable(entityType, stacks);
				ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
				sword.addEnchantment(Enchantments.FIRE_ASPECT, 1);
				builder.optionalParameter(LootContextParameters.TOOL, sword);
				stacks = table.generateLoot(builder.build(lootContextType));
				RandoAssistant.addLootTable(entityType, stacks);
			});
		}
	}
}