package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InteractionMap {
    private static final Codec<Map<String, List<String>>> codec = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING));

    private final BiMap<Input, Output> interactionMap;
    private final Set<Item> knownItems;

    private InteractionMap(BiMap<Input, Output> interactionMap, Set<Item> knownItems) {
        this.interactionMap = interactionMap;
        this.knownItems = knownItems;

        LootTableGraph graph = RandoAssistantClient.lootTableMap.getGraph();
        graph.getExecutor().disableDrawTask();
        interactionMap.forEach((input, output) -> RandoAssistantClient.lootTableMap.getGraph().addInteraction(input.content(), output.content()));
        graph.getExecutor().enableDrawTask();
    }

    public InteractionMap() {
        this(HashBiMap.create(), new HashSet<>());
    }

    public JsonElement serialize() {
        Map<String, List<String>> serializedInteractionMap = new HashMap<>();
        interactionMap.forEach((input, output) -> {
            Set<String> serializedInput = input.serialize();
            Set<String> serializedOutput = output.serialize();
            serializedInteractionMap.put(String.join(",", serializedInput), new ArrayList<>(serializedOutput));
        });
        DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, serializedInteractionMap);
        if(result.error().isPresent()) {
            RandoAssistant.LOGGER.error(result.error().get().message());
        } else if(result.result().isPresent()) {
            return result.result().get();
        }
        RandoAssistant.LOGGER.error("Failed to serialize interaction map");
        RandoAssistant.LOGGER.error("Result: " + result);
        return null;
    }

    public static InteractionMap deserialize(JsonElement json) {
        if(json == null) return new InteractionMap();
        DataResult<Map<String, List<String>>> result = codec.parse(JsonOps.INSTANCE, json);
        if(result.error().isPresent()) {
            RandoAssistant.LOGGER.error(result.error().get().message());
        } else if(result.result().isPresent()) {
            Map<String, List<String>> serializedInteractionMap = result.result().get();
            BiMap<Input, Output> interactionMap = HashBiMap.create();
            Set<Item> knownItems = new HashSet<>();
            for (Map.Entry<String, List<String>> serializedInteraction : serializedInteractionMap.entrySet()) {
                List<String> keys = Arrays.asList(serializedInteraction.getKey().split(","));
                Input input = Input.deserialize(keys);
                Output output = Output.deserialize(serializedInteraction.getValue());
                knownItems.addAll(input.content());
                knownItems.addAll(output.content());
                addInteractionToMap(interactionMap, input, output);
            }
            return new InteractionMap(interactionMap, knownItems);
        }
        RandoAssistant.LOGGER.error("Failed to deserialize interaction map");
        RandoAssistant.LOGGER.error("Result: " + result);
        return null;
    }

    public Set<Map.Entry<Set<Item>, Set<Item>>> getMap() {
        Set<Map.Entry<Set<Item>, Set<Item>>> map = new HashSet<>();
        interactionMap.forEach((input, output) -> map.add(Map.entry(input.content(), output.content())));
        return map;
    }

    public boolean checkInteraction(Item source, Item target) {
        for (Map.Entry<Input, Output> interaction : interactionMap.entrySet()) {
            if (interaction.getKey().content().contains(source) && interaction.getValue().content().contains(target)) {
                return true;
            }
        }
        return false;
    }

    private static void addInteractionToMap(BiMap<Input, Output> interactionMap, Input input, Output output) {
        try {
            boolean added = false;
            for (Input key : interactionMap.keySet()) {
                if (key.content().equals(input.content())) {
                    Output recorded = interactionMap.get(key);
                    HashSet<Item> merged = new HashSet<>(recorded.content());
                    merged.addAll(output.content());
                    interactionMap.put(key, Output.of(merged));
                    added = true;
                    break;
                }
            }
            for (Output value : interactionMap.values()) {
                if (value.content().equals(output.content())) {
                    Input recorded = interactionMap.inverse().get(value);
                    HashSet<Item> merged = new HashSet<>(recorded.content());
                    merged.addAll(input.content());
                    interactionMap.inverse().put(value, Input.of(merged));
                    added = true;
                    break;
                }
            }
            if (!added) {
                interactionMap.put(input, output);
            }
        } catch (IllegalArgumentException e) {
            RandoAssistant.LOGGER.error("Failed to add interaction: " + input + " -> " + output);
        }
    }

    private void addInteraction(Input input, Output output) {
        addInteractionToMap(interactionMap, input, output);
        interactionMap.forEach((in, out) -> {
            knownItems.addAll(in.content);
            knownItems.addAll(out.content);
        });
        RandoAssistantClient.lootTableMap.getGraph().addInteraction(input.content(), output.content());
    }

    public void addInteraction(Item input, Item output) {
        addInteraction(Input.of(input), Output.of(output));
    }

    public void addInteraction(List<Item> input, List<Item> output) {
        addInteraction(Input.of(new HashSet<>(input)), Output.of(new HashSet<>(output)));
    }

    public void addCraftingInteraction(List<Item> input, Item output) {
        input.stream().filter(item -> RandoAssistantClient.lootTableMap.isKnownItem(item) || knownItems.contains(item)).forEach(item -> addInteraction(item, output));
    }

    public void addInteraction(SerializeableInteraction interaction) {
        if(interaction.isCrafting()) {
            addCraftingInteraction(interaction.getInput(), interaction.getOutput().get(0));
        } else {
            addInteraction(interaction.getInput(), interaction.getOutput());
        }
    }

    private record Input(Set<Item> content) implements Iterable<Item> {
        public static Input of(Item input) {
            return new Input(Collections.singleton(input));
        }

        public static Input of(Set<Item> input) {
            return new Input(input);
        }

        public static Input deserialize(List<String> values) {
            Set<Item> input = new HashSet<>();
            values.forEach(item -> input.add(Registries.ITEM.get(new Identifier(item))));
            return new Input(input);
        }

        public Set<String> serialize() {
            Set<String> serialized = new HashSet<>();
            content.forEach(item -> serialized.add(Registries.ITEM.getId(item).toString()));
            return serialized;
        }

        @Override
        public int hashCode() {
            return content.stream().mapToInt(item -> item.getTranslationKey().hashCode()).sum();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Output other)) return false;
            return content.equals(other.content);
        }

        @Override
        public String toString() {
            return content.toString();
        }

        @NotNull
        @Override
        public Iterator<Item> iterator() {
            return content.iterator();
        }
    }

    private record Output(Set<Item> content) implements Iterable<Item> {
        public static Output of(Set<Item> output) {
            return new Output(output);
        }

        public static Output of(Item output) {
            return new Output(Collections.singleton(output));
        }

        public static Output deserialize(List<String> serialized) {
            Set<Item> output = new HashSet<>();
            serialized.forEach(item -> output.add(Registries.ITEM.get(new Identifier(item))));
            return new Output(output);
        }

        public Set<String> serialize() {
            Set<String> serialized = new HashSet<>();
            content.forEach(item -> serialized.add(Registries.ITEM.getId(item).toString()));
            return serialized;
        }

        @Override
        public int hashCode() {
            return content.stream().mapToInt(item -> item.getTranslationKey().hashCode()).sum();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Output other)) return false;
            return content.equals(other.content);
        }

        @Override
        public String toString() {
            return content.toString();
        }

        @NotNull
        @Override
        public Iterator<Item> iterator() {
            return content.iterator();
        }
    }

    @Override
    public String toString() {
        return "InteractionMap{" +
                "interactionMap=" + interactionMap +
                "}";
    }
}
