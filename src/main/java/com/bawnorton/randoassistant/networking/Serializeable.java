package com.bawnorton.randoassistant.networking;

import net.minecraft.util.Identifier;

public interface Serializeable {
    byte[] toBytes();

    Identifier getTypePacket();
}
