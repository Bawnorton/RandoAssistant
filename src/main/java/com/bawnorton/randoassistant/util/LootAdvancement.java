package com.bawnorton.randoassistant.util;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.util.Identifier;

public enum LootAdvancement {
    FIFTY(new Identifier(RandoAssistant.MOD_ID, "50_loottables")),
    HUNDRED(new Identifier(RandoAssistant.MOD_ID, "100_loottables")),
    TWO_HUNDRED(new Identifier(RandoAssistant.MOD_ID, "200_loottables")),
    FIVE_HUNDRED(new Identifier(RandoAssistant.MOD_ID, "500_loottables")),
    ALL(new Identifier(RandoAssistant.MOD_ID, "all_loottables")),
    ALL_BLOCKS(new Identifier(RandoAssistant.MOD_ID, "all_block_loottables")),
    ALL_ENTITIES(new Identifier(RandoAssistant.MOD_ID, "all_entity_loottables")),
    ALL_OTHER(new Identifier(RandoAssistant.MOD_ID, "all_other_loottables"));

    private final Identifier id;

    LootAdvancement(Identifier id) {
        this.id = id;
    }

    public Identifier id() {
        return id;
    }

    public static LootAdvancement fromOrdinal(int ordinal) {
        return LootAdvancement.values()[ordinal];
    }
}
