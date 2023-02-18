package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class InteractionMap {
    private final Map<String, Set<String>> serializedCraftingMap;
    private final Map<Item, List<Item>> interactionMap;

    private InteractionMap(Map<Item, List<Item>> interactionMap) {
        this.interactionMap = interactionMap;
        this.serializedCraftingMap = new HashMap<>();

        LootTableGraph graph = RandoAssistant.lootTableMap.getGraph();
        graph.getDrawer().disable();
        interactionMap.forEach((result, ingredients) -> RandoAssistant.lootTableMap.getGraph().addCraftingRecipe(result, ingredients));
        graph.getDrawer().enable();

        initSerializedCraftingMap();
    }

    public InteractionMap() {
        this(new HashMap<>());
    }

    public static InteractionMap fromSerialized(Map<String, List<String>> serializedCraftingMap) {
        Map<Item, List<Item>> craftingMap = new HashMap<>();

        if (serializedCraftingMap == null) return new InteractionMap();
        for (Map.Entry<String, List<String>> entry : serializedCraftingMap.entrySet()) {
            Item result = Registries.ITEM.get(new Identifier(entry.getKey()));
            List<Item> ingredients = new ArrayList<>();
            entry.getValue().forEach(type -> ingredients.add(Registries.ITEM.get(new Identifier(type))));
            craftingMap.put(result, ingredients);

        }
        return new InteractionMap(craftingMap);
    }

    private void initSerializedCraftingMap() {
        interactionMap.forEach((result, ingredients) -> {
            Set<String> serializedIngredients = new HashSet<>();
            ingredients.forEach(ingredient -> serializedIngredients.add(Registries.ITEM.getId(ingredient).toString()));
            serializedCraftingMap.put(Registries.ITEM.getId(result).toString(), serializedIngredients);
        });
    }

    public List<Item> getIngredients(Item result) {
        return interactionMap.get(result);
    }


    public Set<Map.Entry<Item, List<Item>>> getMap() {
        return interactionMap.entrySet();
    }

    public Map<String, Set<String>> getSerializedCraftingMap() {
        return serializedCraftingMap;
    }

    public void addCrafting(Item output, List<Item> input) {
        interactionMap.put(output, input);
        serializedCraftingMap.put(Registries.ITEM.getId(output).toString(), new HashSet<>());
        RandoAssistant.lootTableMap.getGraph().addCraftingRecipe(output, input);
        input.forEach(item -> serializedCraftingMap.get(Registries.ITEM.getId(output).toString()).add(Registries.ITEM.getId(item).toString()));
    }

    public void addInteraction(Block output, Block input) {
        addCrafting(output.asItem(), Collections.singletonList(input.asItem()));
    }
}
