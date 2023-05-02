package com.bawnorton.randoassistant.stat;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;

public class RandoAssistantStats {
    public static final StatType<Block> INTERACTED = Registry.register(Registries.STAT_TYPE, new Identifier("randoassistant", "interacted_with_block"), new StatType<>(Registries.BLOCK));

    public static void init() {
        RandoAssistant.LOGGER.debug("Initializing RandoAssistantStats");
    }
}
