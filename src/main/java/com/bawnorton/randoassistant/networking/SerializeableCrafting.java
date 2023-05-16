package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Pair;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;

public class SerializeableCrafting implements Serializeable {
    private Pair<String, String> serializedInteraction;
    private Recipe<?> input;
    private Item output;

    private SerializeableCrafting(Recipe<?> input, Item output) {
        this.input = input;
        this.output = output;

        initSerialized();
    }

    public SerializeableCrafting(byte[] bytes) {
        populateData(bytes);
    }

    private void initSerialized() {
        serializedInteraction = new Pair<>(input.getId().toString(), Registries.ITEM.getId(output).toString());
    }

    private void deserialize(Pair<String, String> serialized) {
        this.input = Networking.server.getRecipeManager().get(new Identifier(serialized.a())).orElseThrow();
        this.output = Registries.ITEM.get(new Identifier(serialized.b()));
    }

    public static SerializeableCrafting of(Recipe<?> input, Item output) {
        return new SerializeableCrafting(input, output);
    }

    public Recipe<?> getInput() {
        return input;
    }

    public Item getOutput() {
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
    public void populateData(byte[] bytes) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            deserialize((Pair<String, String>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Identifier getTypePacket() {
        return NetworkingConstants.CRAFTING_PACKET;
    }
}
