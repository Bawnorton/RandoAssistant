package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.util.Identifier;

public class NetworkingConstants {
    public static Identifier BROKE_BLOCK_PACKET = new Identifier(RandoAssistant.MOD_ID, "broke_block");
    public static Identifier LOOT_TABLE_PACKET = new Identifier(RandoAssistant.MOD_ID, "loot_table");
    public static Identifier INTERACTION_PACKET = new Identifier(RandoAssistant.MOD_ID, "interaction");
    public static Identifier UPDATE_DRAWING_PACKET = new Identifier(RandoAssistant.MOD_ID, "update_drawing");
}
