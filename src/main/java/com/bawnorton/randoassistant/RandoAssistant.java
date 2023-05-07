package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<CandleCakeBlock, CandleBlock> CANDLE_CAKE_MAP = new HashMap<>();

    public static void getAllLootTables(PlayerEntity clientPlayer) {
        ServerPlayerEntity serverPlayer = Networking.server.getPlayerManager().getPlayer(clientPlayer.getUuid());
        assert serverPlayer != null;
        serverPlayer.getStatHandler().sendStats(serverPlayer);
        LootManager lootManager = Networking.server.getLootManager();
        LootContextType lootContextType = new LootContextType.Builder().allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL).build();
        LootContext.Builder builder = new LootContext.Builder( Networking.server.getWorld(World.OVERWORLD));
        builder.luck(100f);
        builder.optionalParameter(LootContextParameters.THIS_ENTITY, serverPlayer);

        // will thread this later
        for(int i = 0; i < 100; i++) {
            HashSet<Identifier> seen = new HashSet<>();
            Registries.BLOCK.forEach(block -> {
                seen.add(block.getLootTableId());
                LootTable table = lootManager.getTable(block.getLootTableId());
                List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofBlock(block, stacks));
                ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
                pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
                builder.optionalParameter(LootContextParameters.TOOL, pickaxe);
                stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofBlock(block, stacks));
            });

            Registries.ENTITY_TYPE.forEach(entityType -> {
                seen.add(entityType.getLootTableId());
                Entity entity = entityType.create(Networking.server.getWorld(World.OVERWORLD));
                if (!(entity instanceof LivingEntity)) return;
                LootTable table = lootManager.getTable(entityType.getLootTableId());
                List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofEntity(entityType, stacks));
                ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
                sword.addEnchantment(Enchantments.FIRE_ASPECT, 1);
                builder.optionalParameter(LootContextParameters.TOOL, sword);
                stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofEntity(entityType, stacks));
            });

            lootManager.getTableIds().forEach(id -> {
                if (seen.contains(id)) return;
                LootTable table = lootManager.getTable(id);
                List<ItemStack> stacks = table.generateLoot(builder.build(lootContextType));
                Networking.sendLootTablePacket(serverPlayer, SerializeableLootTable.ofOther(id, stacks));
            });
        }
    }

    public static void getAllInteractions(PlayerEntity clientPlayer) {
        ServerPlayerEntity serverPlayer = Networking.server.getPlayerManager().getPlayer(clientPlayer.getUuid());
        assert serverPlayer != null;
        RecipeManager recipeManager = Networking.server.getRecipeManager();
        recipeManager.values().forEach(recipe -> {
            Item output = recipe.getOutput(DynamicRegistryManager.of(Registries.REGISTRIES)).getItem();
            List<Ingredient> ingredients = recipe.getIngredients();
            List<Item> input = new ArrayList<>();
            ingredients.forEach(ingredient -> {
                for (ItemStack stack : ingredient.getMatchingStacks()) {
                    input.add(stack.getItem());
                }
            });
            Networking.sendInteractionPacket(serverPlayer, SerializeableInteraction.ofCrafting(input, output));
        });

        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach((input, output) -> Networking.sendInteractionPacket(serverPlayer, SerializeableInteraction.ofItemToItem(input.asItem(), output.asItem())));
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().forEach((input, output) -> Networking.sendInteractionPacket(serverPlayer, SerializeableInteraction.ofItemToItem(input.asItem(), output.asItem())));
        AxeItem.STRIPPED_BLOCKS.forEach((input, output) -> Networking.sendInteractionPacket(serverPlayer, SerializeableInteraction.ofItemToItem(input.asItem(), output.asItem())));

        Networking.sendFinishedPacket(serverPlayer);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("RandoAssistant Initialised");
        CommandHandler.init();
        RandoAssistantStats.init();
    }
}