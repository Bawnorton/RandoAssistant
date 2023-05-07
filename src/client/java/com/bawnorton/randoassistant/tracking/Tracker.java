package com.bawnorton.randoassistant.tracking;

import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.tracking.trackable.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Tracker {
    private static Tracker INSTANCE;

    private final TrackableSet<Item, PickedUpTrackable> TRACKABLE_ITEMS;
    private final TrackableSet<Block, MinedTrackable> TRACKABLE_BLOCKS;
    private final TrackableSet<EntityType<?>, KilledTrackable> TRACKABLE_ENTITIES;
    private final TrackableSet<Block, InteractedTrackable> TRACKABLE_INTERACTED;
    private final TrackableSet<Identifier, LootedTrackable> TRACKABLE_LOOTED;

    private final TrackingGraph TRACKED = new TrackingGraph();

    public Tracker() {
        TRACKABLE_ITEMS = new TrackableSet<>();
        TRACKABLE_BLOCKS = new TrackableSet<>();
        TRACKABLE_ENTITIES = new TrackableSet<>();
        TRACKABLE_INTERACTED = new TrackableSet<>();
        TRACKABLE_LOOTED = new TrackableSet<>();
    }

    public static Tracker getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Tracker();
        }
        return INSTANCE;
    }

    public TrackingGraph getGraph() {
        return TRACKED;
    }

    public void track(SerializeableLootTable lootTable) {
        if(lootTable.isBlock()) {
            Block source = lootTable.getBlock();
            List<Item> targets = lootTable.getItems();
            Stat<Block> minedStat = Stats.MINED.getOrCreateStat(source);
            TRACKABLE_BLOCKS.createTrackable(minedStat, MinedTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat, PickedUpTrackable.class);
            });
        } else if (lootTable.isEntity()) {
            EntityType<?> source = lootTable.getEntity();
            List<Item> targets = lootTable.getItems();
            Stat<EntityType<?>> killedStat = Stats.KILLED.getOrCreateStat(source);
            TRACKABLE_ENTITIES.createTrackable(killedStat, KilledTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat, PickedUpTrackable.class);
            });
        } else if (lootTable.isOther()) {
            Identifier source = lootTable.getIdentifier();
            List<Item> targets = lootTable.getItems();
            Stat<Identifier> lootedStat = RandoAssistantStats.LOOTED.getOrCreateStat(source);
            TRACKABLE_LOOTED.createTrackable(lootedStat, LootedTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat, PickedUpTrackable.class);
            });
        } else {
            throw new UnsupportedOperationException();
        }
        List<Identifier> itemIds = lootTable.getItems().stream().map(Registries.ITEM::getId).toList();
        TRACKED.addEdges(lootTable.getIdentifier(), itemIds);
    }
    
    public void track(SerializeableInteraction interaction) {
        List<Item> input = interaction.getInput();
        List<Item> output = interaction.getOutput();
        if(interaction.isCrafting()) {
            Item item = output.get(0);
            Stat<Item> craftedStat = Stats.CRAFTED.getOrCreateStat(item);
            TRACKABLE_ITEMS.createTrackable(craftedStat, PickedUpTrackable.class);
            for(Item inputItem : input) {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(inputItem);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat, PickedUpTrackable.class);
            }
        } else {
            for(Item source : input) {
                Block sourceBlock = Block.getBlockFromItem(source);
                if(sourceBlock == Blocks.AIR) continue;
                Stat<Block> interactedStat = RandoAssistantStats.INTERACTED.getOrCreateStat(sourceBlock);
                TRACKABLE_BLOCKS.createTrackable(interactedStat, MinedTrackable.class);
            }
        }
        List<Identifier> inputIds = input.stream().map(Registries.ITEM::getId).toList();
        List<Identifier> outputIds = output.stream().map(Registries.ITEM::getId).toList();
        TRACKED.addEdges(inputIds, outputIds);
    }

    public List<Trackable<?>> getEnabled() {
        List<Trackable<?>> enabled = new ArrayList<>();
        enabled.addAll(TRACKABLE_ITEMS.getEnabled());
        enabled.addAll(TRACKABLE_BLOCKS.getEnabled());
        enabled.addAll(TRACKABLE_ENTITIES.getEnabled());
        enabled.addAll(TRACKABLE_INTERACTED.getEnabled());
        enabled.addAll(TRACKABLE_LOOTED.getEnabled());
        return enabled;
    }

    public List<Trackable<?>> getDisabled() {
        List<Trackable<?>> disabled = new ArrayList<>();
        disabled.addAll(TRACKABLE_ITEMS.getDisabled());
        disabled.addAll(TRACKABLE_BLOCKS.getDisabled());
        disabled.addAll(TRACKABLE_ENTITIES.getDisabled());
        disabled.addAll(TRACKABLE_INTERACTED.getDisabled());
        disabled.addAll(TRACKABLE_LOOTED.getDisabled());
        return disabled;
    }

    public void enableAll() {
        TRACKABLE_ITEMS.enableAll();
        TRACKABLE_BLOCKS.enableAll();
        TRACKABLE_ENTITIES.enableAll();
        TRACKABLE_INTERACTED.enableAll();
        TRACKABLE_LOOTED.enableAll();
    }

    public void disableAll() {
        TRACKABLE_ITEMS.disableAll();
        TRACKABLE_BLOCKS.disableAll();
        TRACKABLE_ENTITIES.disableAll();
        TRACKABLE_INTERACTED.disableAll();
        TRACKABLE_LOOTED.disableAll();
    }

    private static class TrackableSet<V, T extends Trackable<V>> {
        private final HashMap<Stat<V>, T> trackables;
        
        public TrackableSet(HashMap<Stat<V>, T> trackables) {
            this.trackables = trackables;
        }

        public TrackableSet() {
            this.trackables = new HashMap<>();
        }

        public void createTrackable(Stat<V> stat, Class<T> tClass) {
            if(trackables.containsKey(stat)) {
                trackables.get(stat);
            } else {
                try {
                    trackables.put(stat, tClass.getConstructor(Stat.class).newInstance(stat));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void add(T trackable) {
            trackables.put(trackable.getStat(), trackable);
        }

        public void remove(T trackable) {
            trackables.remove(trackable.getStat());
        }

        public void clear() {
            trackables.clear();
        }

        public HashMap<Stat<V>, T> getTrackables() {
            return trackables;
        }

        public Collection<T> getEnabled() {
            return trackables.values().stream().filter(Trackable::isEnabled).collect(Collectors.toList());
        }

        public Collection<T> getDisabled() {
            return trackables.values().stream().filter(trackable -> !trackable.isEnabled()).collect(Collectors.toList());
        }

        public void enableAll() {
            trackables.values().forEach(Trackable::enableOverride);
        }

        public void disableAll() {
            trackables.values().forEach(Trackable::disableOverride);
        }
    }
}
