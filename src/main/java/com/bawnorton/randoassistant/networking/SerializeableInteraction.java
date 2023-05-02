package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.util.tuples.Triplet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializeableInteraction {
    private Triplet<List<String>, List<String>, Boolean> serializedInteraction;
    private final List<Item> input;
    private final List<Item> output;
    private final boolean isCrafting;

    private SerializeableInteraction(List<Item> input, List<Item> output, boolean isCrafting) {
        this.input = input;
        this.output = output;
        this.isCrafting = isCrafting;

        initSerialized();
    }

    private void initSerialized() {
        List<String> inputNames = new ArrayList<>();
        for(Item item : input) {
            inputNames.add(Registries.ITEM.getId(item).toString());
        }
        List<String> outputNames = new ArrayList<>();
        for(Item item : output) {
            outputNames.add(Registries.ITEM.getId(item).toString());
        }
        serializedInteraction = new Triplet<>(inputNames, outputNames, isCrafting);
    }

    private static SerializeableInteraction deserialize(Triplet<List<String>, List<String>, Boolean> serialized) {
        List<String> inputNames = serialized.a();
        List<String> outputNames = serialized.b();
        boolean isCrafting = serialized.c();
        List<Item> input = new ArrayList<>();
        List<Item> output = new ArrayList<>();
        for(String inputName : inputNames) {
            input.add(Registries.ITEM.get(new Identifier(inputName)));
        }
        for(String outputName : outputNames) {
            output.add(Registries.ITEM.get(new Identifier(outputName)));
        }
        return new SerializeableInteraction(input, output, isCrafting);
    }

    public static SerializeableInteraction ofItemsToItems(List<Item> input, List<Item> output) {
        return new SerializeableInteraction(input, output, false);
    }

    public static SerializeableInteraction ofCrafting(List<Item> input, Item output) {
        return new SerializeableInteraction(input, List.of(output), true);
    }

    public static SerializeableInteraction ofItemToItems(Item input, List<Item> output) {
        return ofItemsToItems(List.of(input), output);
    }

    public static SerializeableInteraction ofItemToItem(Item input, Item output) {
        return ofItemToItems(input, List.of(output));
    }

    public static SerializeableInteraction ofItemToItemStacks(Item input, List<ItemStack> output) {
        List<Item> outputItems = new ArrayList<>();
        for(ItemStack itemStack : output) {
            outputItems.add(itemStack.getItem());
        }
        return ofItemToItems(input, outputItems);
    }

    public static SerializeableInteraction ofBlockToBlock(Block input, Block output) {
        return ofItemToItem(input.asItem(), output.asItem());
    }

    public List<Item> getInput() {
        return input;
    }

    public List<Item> getOutput() {
        return output;
    }

    public boolean isCrafting() {
        return isCrafting;
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

    public static SerializeableInteraction fromBytes(byte[] bytes) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return deserialize((Triplet<List<String>, List<String>, Boolean>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
