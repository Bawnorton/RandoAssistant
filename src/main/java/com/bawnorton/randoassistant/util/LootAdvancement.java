package com.bawnorton.randoassistant.util;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public enum LootAdvancement {
    FIFTY(new Identifier(RandoAssistant.MOD_ID, "50_loottables")),
    HUNDRED(new Identifier(RandoAssistant.MOD_ID, "100_loottables")),
    TWO_HUNDRED(new Identifier(RandoAssistant.MOD_ID, "200_loottables")),
    FIVE_HUNDRED(new Identifier(RandoAssistant.MOD_ID, "500_loottables")),
    ALL_CANDLES(new Identifier(RandoAssistant.MOD_ID, "all_candles")),
    ALL_CHESTS(new Identifier(RandoAssistant.MOD_ID, "all_chests")),
    ALL_VILLAGER_GIFTS(new Identifier(RandoAssistant.MOD_ID, "all_villager_gifts")),
    ALL(new Identifier(RandoAssistant.MOD_ID, "all_loottables")),
    ALL_BLOCKS(new Identifier(RandoAssistant.MOD_ID, "all_block_loottables")),
    ALL_ENTITIES(new Identifier(RandoAssistant.MOD_ID, "all_entity_loottables")),
    ALL_OTHER(new Identifier(RandoAssistant.MOD_ID, "all_other_loottables")),
    CAT_MORNING_GIFT(new Identifier(RandoAssistant.MOD_ID, "cat_morning_gift")),
    SHAME(new Identifier(RandoAssistant.MOD_ID, "shame")),
    MONSTER(new Identifier(RandoAssistant.MOD_ID, "monster")),
    ORPHANED_POLAR_BEAR(new Identifier(RandoAssistant.MOD_ID, "orphaned_polar_bear")),
    WOB(new Identifier(RandoAssistant.MOD_ID, "wob")),
    DEB(new Identifier(RandoAssistant.MOD_ID, "deb"));

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

    public void grant(ServerPlayerEntity serverPlayer) {
        MinecraftServer server = serverPlayer.getServer();
        if(server == null) return;

        AdvancementEntry advancement = server.getAdvancementLoader().get(id());
        if(advancement == null) return;

        AdvancementProgress progress = serverPlayer.getAdvancementTracker().getProgress(advancement);
        if(progress.isDone()) return;

        for(String criterion : progress.getUnobtainedCriteria()) {
            serverPlayer.getAdvancementTracker().grantCriterion(advancement, criterion);
        }
    }
}
