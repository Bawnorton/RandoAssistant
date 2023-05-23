package com.bawnorton.randoassistant.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface Serializeable {
    PacketByteBuf serialize();
    Identifier getTypePacket();
}
