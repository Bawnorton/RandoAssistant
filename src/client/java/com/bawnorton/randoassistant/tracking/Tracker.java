package com.bawnorton.randoassistant.tracking;

import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Tracker {
    private static Tracker INSTANCE;

    private final TrackableMap<Identifier> TRACKABLE_INTERACTED;
    private final TrackableMap<Identifier> TRACKABLE_LOOTED;

    private final Map<Identifier, Set<Trackable<Identifier>>> TRACKED;

    public Tracker() {
        TRACKABLE_INTERACTED = new TrackableMap<>();
        TRACKABLE_LOOTED = new TrackableMap<>();
        TRACKED = Maps.newHashMap();
    }

    public static Tracker getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Tracker();
        }
        return INSTANCE;
    }

    public void track(SerializeableLootTable lootTable) {
        Identifier lootTableId = lootTable.getLootTableId();
        Identifier sourceId = lootTable.getSourceId();
        List<Item> targets = lootTable.getItems();
        Stat<Identifier> stat = RandoAssistantStats.LOOTED.getOrCreateStat(lootTableId);
        Trackable<Identifier> trackable = TRACKABLE_LOOTED.getOrCreate(stat, sourceId);
        for(Item target : targets) {
            trackable.addOutput(Registries.ITEM.getId(target));
            Set<Trackable<Identifier>> tracked = TRACKED.getOrDefault(Registries.ITEM.getId(target), Sets.newHashSet());
            tracked.add(trackable);
            TRACKED.put(Registries.ITEM.getId(target), tracked);
        }
    }
    
    public void track(SerializeableInteraction interaction) {
        List<Item> input = interaction.getInput();
        List<Item> output = interaction.getOutput();
        for(Item source : input) {
            Stat<Identifier> stat = RandoAssistantStats.INTERACTED.getOrCreateStat(Registries.ITEM.getId(source));
            Trackable<Identifier> trackable = TRACKABLE_INTERACTED.getOrCreate(stat, Registries.ITEM.getId(source));
            for(Item target : output) {
                trackable.addOutput(Registries.ITEM.getId(target));
                Set<Trackable<Identifier>> tracked = TRACKED.getOrDefault(Registries.ITEM.getId(target), Sets.newHashSet());
                tracked.add(trackable);
                TRACKED.put(Registries.ITEM.getId(target), tracked);
            }
        }
    }

    @Nullable
    public Set<Trackable<Identifier>> getTracked(Identifier id) {
        return TRACKED.get(id);
    }

    @SafeVarargs
    public final Set<Trackable<?>> getEnabled(Predicate<Trackable<?>>... filters) {
        Set<Trackable<?>> enabled = Sets.newHashSet();
        enabled.addAll(TRACKABLE_INTERACTED.getEnabled(filters));
        enabled.addAll(TRACKABLE_LOOTED.getEnabled(filters));
        return enabled;
    }

    public void enableAll() {
        TRACKABLE_INTERACTED.enableAll();
        TRACKABLE_LOOTED.enableAll();
    }

    public void disableAll() {
        TRACKABLE_INTERACTED.disableAll();
        TRACKABLE_LOOTED.disableAll();
    }

    private static class TrackableMap<T> {
        private final HashMap<Stat<T>, Trackable<T>> trackables;

        public TrackableMap() {
            this.trackables = new HashMap<>();
        }

        public Trackable<T> getOrCreate(Stat<T> stat, Identifier sourceId) {
            if(trackables.containsKey(stat)) {
                return trackables.get(stat);
            } else {
                Trackable<T> trackable = new Trackable<>(stat, sourceId);
                trackables.put(stat, trackable);
                return trackable;
            }
        }

        @SafeVarargs
        public final Set<Trackable<T>> getEnabled(Predicate<Trackable<?>>... filters) {
            Set<Trackable<T>> enabled = Sets.newHashSet();
            for(Trackable<T> trackable : trackables.values()) {
                if(trackable.isEnabled()) {
                    boolean passes = true;
                    for(Predicate<Trackable<?>> filter : filters) {
                        if(!filter.test(trackable)) {
                            passes = false;
                            break;
                        }
                    }
                    if(passes) {
                        enabled.add(trackable);
                    }
                }
            }
            return enabled;
        }

        public void enableAll() {
            trackables.values().forEach(Trackable::enableOverride);
        }

        public void disableAll() {
            trackables.values().forEach(Trackable::disableOverride);
        }
    }
}
