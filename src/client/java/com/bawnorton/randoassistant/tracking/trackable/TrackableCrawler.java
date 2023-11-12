package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.stat.StatsManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.util.RecipeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

// class that crawls up a trackable tree and builds a graph of the trackables it finds
public abstract class TrackableCrawler {
    private static final Map<Identifier, TrackingGraph> sourceGraphCache = new HashMap<>();
    private static final Map<Identifier, TrackingGraph> targetGraphCache = new HashMap<>();

    public static TrackingGraph crawlUp(Identifier target) {
        if(sourceGraphCache.containsKey(target)) {
            return sourceGraphCache.get(target);
        }
        TrackingGraph graph = new TrackingGraph();
        crawlUp(target, target, graph, 0);
        sourceGraphCache.put(target, graph);
        return graph;
    }

    private static void crawlUp(Identifier original, Identifier target, TrackingGraph graph, int depth) {
        if(depth >= Config.getInstance().searchDepth) return;
        graph.add(target);
        Set<Trackable<Identifier>> sources = Tracker.getInstance().getSources(target);
        List<Identifier> sourceIds = getIds(sources);
        for(Identifier sourceId : sourceIds) {
            if(sourceId.equals(original)) continue;
            graph.connect(sourceId, target);
            crawlUp(original, sourceId, graph, depth + 1);
        }

        getRecipe(target).ifPresent(recipe -> getRecipeId(recipe).ifPresent(recipeId -> {
            graph.connect(recipeId, target);
            List<Item> items = recipe.value().getIngredients().stream().map(Ingredient::getMatchingStacks).flatMap(Arrays::stream).map(ItemStack::getItem).toList();
            for(Item item: items) {
                if(!Tracker.getInstance().hasObtained(item)) continue;
                Identifier itemId = Registries.ITEM.getId(item);
                if(itemId.equals(original)) continue;
                graph.connect(itemId, recipeId);
                crawlUp(original, itemId, graph, depth + 2);
            }
        }));
    }

    public static TrackingGraph crawlDown(Identifier target) {
        if(targetGraphCache.containsKey(target)) {
            return targetGraphCache.get(target);
        }
        TrackingGraph graph = new TrackingGraph();
        crawlDown(target, target, graph, 0);
        targetGraphCache.put(target, graph);
        return graph;
    }

    public static void crawlDown(Identifier original, Identifier target, TrackingGraph graph, int depth) {
        if(depth >= Config.getInstance().searchDepth) return;
        graph.add(target);
        Set<Trackable<Identifier>> sources = Tracker.getInstance().getTargets(target);
        List<Identifier> sourceIds = getIds(sources);
        for(Identifier sourceId : sourceIds) {
            if(sourceId.equals(original)) continue;
            graph.connect(sourceId, target);
            crawlDown(original, sourceId, graph, depth + 1);
        }
        getRecipe(target).ifPresent(recipe -> getRecipeId(recipe).ifPresent(recipeId -> {
            graph.connect(recipeId, target);
            List<Item> items = recipe.value().getIngredients().stream().map(Ingredient::getMatchingStacks).flatMap(Arrays::stream).map(ItemStack::getItem).toList();
            for(Item item: items) {
                if(!Tracker.getInstance().hasObtained(item)) continue;
                Identifier itemId = Registries.ITEM.getId(item);
                if(itemId.equals(original)) continue;
                graph.connect(itemId, recipeId);
                crawlDown(original, itemId, graph, depth + 2);
            }
        }));
    }

    private static List<Identifier> getIds(Set<Trackable<Identifier>> sources) {
        if(sources == null) sources = new HashSet<>();

        return sources.stream().filter(trackable -> {
            if(trackable.isEnabled()) {
                if(!Config.getInstance().enableInteractions) {
                    return !StatsManager.INTERACTED.hasStat(trackable.getStat().getValue());
                }
                return true;
            }
            return false;
        }).map(Trackable::getIdentifier).toList();
    }

    private static Optional<? extends RecipeEntry<?>> getRecipe(Identifier target) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return Optional.empty();

        RecipeManager recipeManager = networkHandler.getRecipeManager();
        return recipeManager.get(target);

    }

    private static Optional<Identifier> getRecipeId(RecipeEntry<?> recipe) {
        if (!Tracker.getInstance().hasCrafted(recipe) || !Config.getInstance().enableCrafting) return Optional.empty();

        RecipeType recipeType = RecipeType.fromRecipe(recipe);
        return Optional.of(new Identifier(recipe.id().getNamespace(), recipeType.getName() + recipe.id().getPath()));
    }

    public static void clearCache() {
        sourceGraphCache.clear();
        targetGraphCache.clear();
    }
}
