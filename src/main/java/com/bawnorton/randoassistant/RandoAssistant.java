package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.item.Wob;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableCrafting;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item WOB = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "wob"), new Wob(new FabricItemSettings()));

    public static void getAllLootTables(ServerPlayerEntity player, MinecraftServer server) {
        player.getStatHandler().sendStats(player);
        LootManager lootManager = server.getLootManager();
        LootContextType lootContextType = new LootContextType.Builder().allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL).build();

        for(int i = 0; i < 50; i++) {
            HashSet<Identifier> seen = new HashSet<>();
            Registries.BLOCK.forEach(block -> {
                LootContext.Builder builder = new LootContext.Builder(server.getWorld(World.OVERWORLD));
                builder.luck(100f);
                builder.optionalParameter(LootContextParameters.THIS_ENTITY, player);

                seen.add(block.getLootTableId());
                LootTable table = lootManager.getTable(block.getLootTableId());
                Set<ItemStack> stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofBlock(block, stacks, false));

                ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
                pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                builder.optionalParameter(LootContextParameters.TOOL, pickaxe);
                stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofBlock(block, stacks, true));
            });

            Registries.ENTITY_TYPE.forEach(entityType -> {
                LootContext.Builder builder = new LootContext.Builder(server.getWorld(World.OVERWORLD));
                builder.luck(100f);
                builder.optionalParameter(LootContextParameters.THIS_ENTITY, player);

                seen.add(entityType.getLootTableId());
                Entity entity = entityType.create(server.getWorld(World.OVERWORLD));
                if (!(entity instanceof LivingEntity)) return;
                LootTable table = lootManager.getTable(entityType.getLootTableId());
                Set<ItemStack> stacks = new HashSet<>(table.generateLoot(builder.build(lootContextType)));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofEntity(entityType, stacks));
            });

            lootManager.getTableIds().forEach(id -> {
                if (seen.contains(id)) return;
                LootContext.Builder builder = new LootContext.Builder(server.getWorld(World.OVERWORLD));
                builder.luck(100f);
                builder.optionalParameter(LootContextParameters.THIS_ENTITY, player);

                LootTable table = lootManager.getTable(id);
                List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendSerializeablePacket(player, SerializeableLootTable.ofOther(id, stacks));
            });
        }
    }

    public static void getAllInteractions(ServerPlayerEntity player) {
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        OxidizableBlock.OXIDATION_LEVEL_DECREASES.get().forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
        AxeItem.STRIPPED_BLOCKS.forEach((input, output) -> Networking.sendSerializeablePacket(player, SerializeableInteraction.of(input, output)));
    }

    public static void getAllRecipes(ServerPlayerEntity player, MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        recipeManager.values().forEach(recipe -> {
            Item output = recipe.getOutput(DynamicRegistryManager.of(Registries.REGISTRIES)).getItem();
            Networking.sendSerializeablePacket(player, SerializeableCrafting.of(recipe, output));
        });
    }

    @Override
    public void onInitialize() {
        LOGGER.info("RandoAssistant Initialised");
        CommandHandler.init();
        RandoAssistantStats.init();
        EventManager.init();
        Networking.init();
    }
}