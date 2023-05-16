package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.util.RecipeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

// class that crawls up a trackable tree and builds a graph of the trackables it finds
public abstract class TrackableCrawler {
    private static final Map<Identifier, TrackingGraph> graphCache = new HashMap<>();

    public static TrackingGraph crawl(Identifier target) {
        if(graphCache.containsKey(target)) {
            return graphCache.get(target);
        }
        TrackingGraph graph = new TrackingGraph();
        crawl(target, target, graph, 0);
        graphCache.put(target, graph);
        return graph;
    }

    private static void crawl(Identifier original, Identifier target, TrackingGraph graph, int depth) {
        if(depth >= Config.getInstance().searchDepth) return;
        graph.add(target);
        Set<Trackable<Identifier>> sources = Tracker.getInstance().getTracked(target);
        if(sources == null) sources = new HashSet<>();

        List<Identifier> sourceIds = sources.stream().filter(Trackable::isEnabled).map(Trackable::getIdentifier).toList();
        for(Identifier sourceId : sourceIds) {
            if(sourceId.equals(original)) continue;
            graph.connect(sourceId, target);
            crawl(original, sourceId, graph, depth + 1);
        }

        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return;

        RecipeManager recipeManager = networkHandler.getRecipeManager();
        Optional<? extends Recipe<?>> optional = recipeManager.get(target);
        if (optional.isEmpty()) return;

        Recipe<?> recipe = optional.get();
        if (!Tracker.getInstance().hasCrafted(recipe)) return;

        RecipeType recipeType = RecipeType.fromRecipe(recipe);
        Identifier recipeId = new Identifier(recipe.getId().getNamespace(), recipeType.getName() + recipe.getId().getPath());
        graph.connect(recipeId, target);
        List<Item> items = recipe.getIngredients().stream().map(Ingredient::getMatchingStacks).flatMap(Arrays::stream).map(ItemStack::getItem).toList();
        for(Item item: items) {
            if(!Tracker.getInstance().hasObtained(item)) continue;
            Identifier itemId = Registries.ITEM.getId(item);
            if(itemId.equals(original)) continue;
            graph.connect(itemId, recipeId);
            crawl(original, itemId, graph, depth + 2);
        }
    }

    public static void clearCache() {
        graphCache.clear();
    }
}
