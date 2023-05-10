package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.util.Identifier;

public class NetworkingConstants {
    public static final Identifier LOOT_TABLE_PACKET = new Identifier(RandoAssistant.MOD_ID, "loot_table");
    public static final Identifier INTERACTION_PACKET = new Identifier(RandoAssistant.MOD_ID, "interaction");
    public static final Identifier ENABLE_ALL_PACKET = new Identifier(RandoAssistant.MOD_ID, "enable_all");
    public static final Identifier DISABLE_ALL_PACKET = new Identifier(RandoAssistant.MOD_ID, "disable_all");
    public static final Identifier CLEAR_CACHE_PACKET = new Identifier(RandoAssistant.MOD_ID, "clear_cache");
    public static final Identifier DEBUG_PACKET = new Identifier(RandoAssistant.MOD_ID, "debug");
}
