package com.bawnorton.randoassistant.networking;

import net.minecraft.util.Identifier;

public interface Serializeable {
    byte[] toBytes();
    void populateData(byte[] bytes);
    Identifier getTypePacket();
}
