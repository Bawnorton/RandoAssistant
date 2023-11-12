package com.bawnorton.randoassistant.stat;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StatsManager {
    public static final StatType<Identifier> INTERACTED = registerType("interacted_with_block", Registries.CUSTOM_STAT);
    public static final StatType<Identifier> LOOTED = registerType("looted_block", Registries.CUSTOM_STAT);
    public static final StatType<Identifier> CRAFTED = registerType("crafted_item", Registries.CUSTOM_STAT);
    public static final StatType<Block> SILK_TOUCHED = registerType("silk_touched_block", Registries.BLOCK);

    private static <T> StatType<T> registerType(String id, Registry<T> registry) {
        MutableText text = Text.translatable("stat_type.randoassistant." + id);
        Identifier identifier = new Identifier(RandoAssistant.MOD_ID, id);
        return Registry.register(Registries.STAT_TYPE, identifier, new StatType<>(registry, text));
    }
    
    public static void init() {
        RandoAssistant.LOGGER.debug("Initializing StatsManager");
    }

    public static boolean isCustom(Stat<?> stat) {
        return stat.getType().equals(INTERACTED) || stat.getType().equals(LOOTED) || stat.getType().equals(SILK_TOUCHED) || stat.getType().equals(CRAFTED);
    }

    public static boolean usesIdentifier(Stat<?> stat) {
        return stat.getType().equals(INTERACTED) || stat.getType().equals(LOOTED) || stat.getType().equals(CRAFTED);
    }
}
