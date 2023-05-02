package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
        CommandHandler.init();
    }
}