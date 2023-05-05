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

import java.util.*;
import java.util.stream.Collectors;

public class Tracker {
    private static Tracker INSTANCE;

    private final TrackableSet<Item, PickedUpTrackable> TRACKABLE_ITEMS;
    private final TrackableSet<Block, MinedTrackable> TRACKABLE_BLOCKS;
    private final TrackableSet<EntityType<?>, KilledTrackable> TRACKABLE_ENTITIES;
    private final HashMap<Identifier, CustomTrackable> TRACKABLE_CUSTOM;

    private final TrackingGraph TRACKED = TrackingGraph.getInstance();

    public Tracker() {
        TRACKABLE_ITEMS = new TrackableSet<>();
        TRACKABLE_BLOCKS = new TrackableSet<>();
        TRACKABLE_ENTITIES = new TrackableSet<>();
        TRACKABLE_CUSTOM = new HashMap<>();
    }

    public static Tracker getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Tracker();
        }
        return INSTANCE;
    }

    public void track(SerializeableLootTable lootTable) {
        if(lootTable.isBlock()) {
            Block source = lootTable.getBlock();
            List<Item> targets = lootTable.getItems();
            Stat<Block> minedStat = Stats.MINED.getOrCreateStat(source);
            TRACKABLE_BLOCKS.createTrackable(minedStat);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat);
            });
        } else if (lootTable.isEntity()) {
            EntityType<?> source = lootTable.getEntity();
            List<Item> targets = lootTable.getItems();
            Stat<EntityType<?>> killedStat = Stats.KILLED.getOrCreateStat(source);
            TRACKABLE_ENTITIES.createTrackable(killedStat);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat);
            });
        } else if (lootTable.isOther()) {
            Identifier source = lootTable.getIdentifier();
            List<Item> targets = lootTable.getItems();
            createCustomTrackable(source);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat);
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
            TRACKABLE_ITEMS.createTrackable(craftedStat);
            for(Item inputItem : input) {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(inputItem);
                TRACKABLE_ITEMS.createTrackable(pickedUpStat);
            }
        } else {
            for(Item source : input) {
                Block sourceBlock = Block.getBlockFromItem(source);
                if(sourceBlock == Blocks.AIR) continue;
                Stat<Block> interactedStat = RandoAssistantStats.INTERACTED.getOrCreateStat(sourceBlock);
                TRACKABLE_BLOCKS.createTrackable(interactedStat);
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
        return enabled;
    }

    public List<CustomTrackable> getCustomEnabled() {
        return TRACKABLE_CUSTOM.values().stream().filter(CustomTrackable::isEnabled).collect(Collectors.toList());
    }

    private void createCustomTrackable(Identifier identifier) {
        CustomTrackable customTrackable = TRACKABLE_CUSTOM.get(identifier);
        if(customTrackable == null) {
            customTrackable = new CustomTrackable(identifier);
            TRACKABLE_CUSTOM.put(identifier, customTrackable);
        }
    }

    public void enableAll() {
        TRACKABLE_ITEMS.enableAll();
        TRACKABLE_BLOCKS.enableAll();
        TRACKABLE_ENTITIES.enableAll();
        TRACKABLE_CUSTOM.values().forEach(CustomTrackable::enable);
    }

    public void disableAll() {
        TRACKABLE_ITEMS.disableAll();
        TRACKABLE_BLOCKS.disableAll();
        TRACKABLE_ENTITIES.disableAll();
        TRACKABLE_CUSTOM.values().forEach(CustomTrackable::disable);
    }

    private static class TrackableSet<V, T extends Trackable<V>> {
        private final HashMap<Stat<V>, T> trackables;
        
        public TrackableSet(HashMap<Stat<V>, T> trackables) {
            this.trackables = trackables;
        }

        public TrackableSet() {
            this.trackables = new HashMap<>();
        }

        public void createTrackable(Stat<V> stat) {
            if(trackables.containsKey(stat)) {
                trackables.get(stat);
            } else {
                T trackable = Trackable.of(stat);
                trackables.put(stat, trackable);
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

        public void enableAll() {
            trackables.values().forEach(Trackable::enableOverride);
        }

        public void disableAll() {
            trackables.values().forEach(Trackable::disableOverride);
        }
    }
}
