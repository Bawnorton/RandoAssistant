package com.bawnorton.randoassistant.tracking;

import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.bawnorton.randoassistant.tracking.trackable.*;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import javax.sound.midi.Track;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Tracker {
    private static Tracker INSTANCE;

    private final TrackableMap<Item, PickedUpTrackable> TRACKABLE_ITEMS;
    private final TrackableMap<Block, MinedTrackable> TRACKABLE_BLOCKS;
    private final TrackableMap<EntityType<?>, KilledTrackable> TRACKABLE_ENTITIES;
    private final TrackableMap<Block, InteractedTrackable> TRACKABLE_INTERACTED;
    private final TrackableMap<Identifier, LootedTrackable> TRACKABLE_LOOTED;

    public Tracker() {
        TRACKABLE_ITEMS = new TrackableMap<>();
        TRACKABLE_BLOCKS = new TrackableMap<>();
        TRACKABLE_ENTITIES = new TrackableMap<>();
        TRACKABLE_INTERACTED = new TrackableMap<>();
        TRACKABLE_LOOTED = new TrackableMap<>();
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
            MinedTrackable trackableSource = TRACKABLE_BLOCKS.getOrCreate(minedStat, MinedTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.getOrCreate(pickedUpStat, PickedUpTrackable.class).addSource(trackableSource);
            });
        } else if (lootTable.isEntity()) {
            EntityType<?> source = lootTable.getEntity();
            List<Item> targets = lootTable.getItems();
            Stat<EntityType<?>> killedStat = Stats.KILLED.getOrCreateStat(source);
            KilledTrackable trackableSource = TRACKABLE_ENTITIES.getOrCreate(killedStat, KilledTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.getOrCreate(pickedUpStat, PickedUpTrackable.class).addSource(trackableSource);
            });
        } else if (lootTable.isOther()) {
            Identifier source = lootTable.getIdentifier();
            List<Item> targets = lootTable.getItems();
            Stat<Identifier> lootedStat = RandoAssistantStats.LOOTED.getOrCreateStat(source);
            LootedTrackable trackableSource = TRACKABLE_LOOTED.getOrCreate(lootedStat, LootedTrackable.class);
            targets.forEach(item -> {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(item);
                TRACKABLE_ITEMS.getOrCreate(pickedUpStat, PickedUpTrackable.class).addSource(trackableSource);
            });
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public void track(SerializeableInteraction interaction) {
        List<Item> input = interaction.getInput();
        List<Item> output = interaction.getOutput();
        if(interaction.isCrafting()) {
            Item item = output.get(0);
            Stat<Item> craftedStat = Stats.CRAFTED.getOrCreateStat(item);
            PickedUpTrackable trackableTarget = TRACKABLE_ITEMS.getOrCreate(craftedStat, PickedUpTrackable.class);
            for(Item inputItem : input) {
                Stat<Item> pickedUpStat = Stats.PICKED_UP.getOrCreateStat(inputItem);
                trackableTarget.addSource(TRACKABLE_ITEMS.getOrCreate(pickedUpStat, PickedUpTrackable.class));
            }
        } else {
            for(Item source : input) {
                Block sourceBlock = Block.getBlockFromItem(source);
                if(sourceBlock == Blocks.AIR) continue;
                Stat<Block> interactedStat = RandoAssistantStats.INTERACTED.getOrCreateStat(sourceBlock);
                TRACKABLE_INTERACTED.getOrCreate(interactedStat, InteractedTrackable.class);
            }
        }
    }

    public Set<Trackable<?>> getEnabled(Predicate<Trackable<?>> filter) {
        Set<Trackable<?>> enabled = Sets.newHashSet();
        enabled.addAll(TRACKABLE_ITEMS.getEnabled(filter));
        enabled.addAll(TRACKABLE_BLOCKS.getEnabled(filter));
        enabled.addAll(TRACKABLE_ENTITIES.getEnabled(filter));
        enabled.addAll(TRACKABLE_INTERACTED.getEnabled(filter));
        enabled.addAll(TRACKABLE_LOOTED.getEnabled(filter));
        return enabled;
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

    private static class TrackableMap<V, T extends Trackable<V>> {
        private final HashMap<Stat<V>, T> trackables;

        public TrackableMap() {
            this.trackables = new HashMap<>();
        }

        public T getOrCreate(Stat<V> stat, Class<T> tClass) {
            if(trackables.containsKey(stat)) {
                return trackables.get(stat);
            } else {
                try {
                    T trackable = tClass.getConstructor(Stat.class).newInstance(stat);
                    trackables.put(stat, trackable);
                    return trackable;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public Set<T> getEnabled(Predicate<? super T> filter) {
            return trackables.values().stream().filter(Trackable::isEnabled).filter(filter).collect(Collectors.toSet());
        }

        public void enableAll() {
            trackables.values().forEach(Trackable::enableOverride);
        }

        public void disableAll() {
            trackables.values().forEach(Trackable::disableOverride);
        }
    }
}
