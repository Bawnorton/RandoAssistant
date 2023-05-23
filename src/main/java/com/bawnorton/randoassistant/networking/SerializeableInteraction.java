package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Pair;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;

public class SerializeableInteraction implements Serializeable {
    private final Block input;
    private final Block output;

    private SerializeableInteraction(Block input, Block output) {
        this.input = input;
        this.output = output;
    }

    public static SerializeableInteraction deserialize(PacketByteBuf buf) {
        Block input = Registries.BLOCK.get(buf.readIdentifier());
        Block output = Registries.BLOCK.get(buf.readIdentifier());
        return new SerializeableInteraction(input, output);
    }

    public PacketByteBuf serialize() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(Registries.BLOCK.getId(input));
        buf.writeIdentifier(Registries.BLOCK.getId(output));
        return buf;
    }

    public static SerializeableInteraction of(Block input, Block output) {
        return new SerializeableInteraction(input, output);
    }

    public Block getInput() {
        return input;
    }

    public Block getOutput() {
        return output;
    }

    @Override
    public Identifier getTypePacket() {
        return NetworkingConstants.INTERACTION_PACKET;
    }
}
