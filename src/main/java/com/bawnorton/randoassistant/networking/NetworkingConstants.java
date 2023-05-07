package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.util.Identifier;

public class NetworkingConstants {
    public static final Identifier LOOT_TABLE_PACKET = new Identifier(RandoAssistant.MOD_ID, "loot_table");
    public static final Identifier INTERACTION_PACKET = new Identifier(RandoAssistant.MOD_ID, "interaction");
    public static final Identifier FINISHED_PACKET = new Identifier(RandoAssistant.MOD_ID, "finished");
}
