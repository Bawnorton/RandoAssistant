package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Pair;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;

public class SerializeableInteraction implements Serializeable {
    private Pair<String, String> serializedInteraction;
    private Block input;
    private Block output;

    private SerializeableInteraction(Block input, Block output) {
        this.input = input;
        this.output = output;

        initSerialized();
    }

    public SerializeableInteraction(byte[] bytes) {
        populateData(bytes);
    }

    private void initSerialized() {
        serializedInteraction = new Pair<>(Registries.BLOCK.getId(input).toString(), Registries.BLOCK.getId(output).toString());
    }

    private void deserialize(Pair<String, String> serialized) {
        this.input = Registries.BLOCK.get(new Identifier(serialized.a()));
        this.output = Registries.BLOCK.get(new Identifier(serialized.b()));
    }

    public static SerializeableInteraction ofBlockToBlock(Block input, Block output) {
        return new SerializeableInteraction(input, output);
    }

    public Block getInput() {
        return input;
    }

    public Block getOutput() {
        return output;
    }

    public byte[] toBytes() {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(serializedInteraction);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateData(byte[] bytes) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            deserialize((Pair<String, String>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Identifier getTypePacket() {
        return NetworkingConstants.INTERACTION_PACKET;
    }
}
