package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.util.Identifier;

public class NetworkingConstants {
    public static final Identifier LOOT_TABLE_PACKET = new Identifier(RandoAssistant.MOD_ID, "loot_table");
    public static final Identifier INTERACTION_PACKET = new Identifier(RandoAssistant.MOD_ID, "interaction");
    public static final Identifier CRAFTING_PACKET = new Identifier(RandoAssistant.MOD_ID, "crafting");
    public static final Identifier CLEAR_CACHE_PACKET = new Identifier(RandoAssistant.MOD_ID, "clear_cache");
    public static final Identifier DEBUG_PACKET = new Identifier(RandoAssistant.MOD_ID, "debug");
    public static final Identifier HANDSHAKE_PACKET = new Identifier(RandoAssistant.MOD_ID, "handshake");
    public static final Identifier STATS_PACKET = new Identifier(RandoAssistant.MOD_ID, "stats");
    public static final Identifier ADVANCEMENT_UNLOCK_PACKET = new Identifier(RandoAssistant.MOD_ID, "advancement_unlock");
    public static final Identifier CANDLE_LOOT_PACKET = new Identifier(RandoAssistant.MOD_ID, "candle_loot");
}
