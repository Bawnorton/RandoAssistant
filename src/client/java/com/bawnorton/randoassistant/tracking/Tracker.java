package com.bawnorton.randoassistant.tracking;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.screen.LootBookWidget;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tracker {
    private static Tracker INSTANCE;

    private final TrackableMap<Identifier> TRACKABLE_INTERACTED;
    private final TrackableMap<Identifier> TRACKABLE_CRAFTED;
    private final TrackableMap<Identifier> TRACKABLE_LOOTED;

    private final Map<Identifier, Set<Trackable<Identifier>>> TRACKED;

    public Tracker() {
        TRACKABLE_INTERACTED = new TrackableMap<>();
        TRACKABLE_CRAFTED = new TrackableMap<>();
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
            Trackable<Identifier> trackable = (interaction.isCrafting() ? TRACKABLE_CRAFTED : TRACKABLE_INTERACTED).getOrCreate(stat, Registries.ITEM.getId(source));
            for(Item target : output) {
                trackable.addOutput(Registries.ITEM.getId(target));
                Set<Trackable<Identifier>> tracked = TRACKED.getOrDefault(Registries.ITEM.getId(target), Sets.newHashSet());
                tracked.add(trackable);
                TRACKED.put(Registries.ITEM.getId(target), tracked);
            }
        }
    }

    public boolean hasCrafted(Item item) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        int count = player.getStatHandler().getStat(Stats.CRAFTED.getOrCreateStat(item));
        return count > 0;
    }

    public boolean hasSilkTouched(Block block) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        int count = player.getStatHandler().getStat(RandoAssistantStats.SILK_TOUCHED.getOrCreateStat(block));
        return count > 0;
    }

    @Nullable
    public Set<Trackable<Identifier>> getTracked(Identifier id) {
        return TRACKED.get(id);
    }

    public final Set<Trackable<Identifier>> getEnabledLooted() {
        return TRACKABLE_LOOTED.getEnabled();
    }

    public final Set<Trackable<Identifier>> getEnabledInteracted() {
        return TRACKABLE_INTERACTED.getEnabled();
    }

    public final Set<Trackable<Identifier>> getEnabledCrafted() {
        return TRACKABLE_CRAFTED.getEnabled();
    }

    public void enableAll() {
        TRACKABLE_INTERACTED.enableAll();
        TRACKABLE_CRAFTED.enableAll();
        TRACKABLE_LOOTED.enableAll();
    }

    public void disableAll() {
        TRACKABLE_INTERACTED.disableAll();
        TRACKABLE_CRAFTED.disableAll();
        TRACKABLE_LOOTED.disableAll();
    }

    public void clear() {
        TRACKABLE_INTERACTED.clear();
        TRACKABLE_CRAFTED.clear();
        TRACKABLE_LOOTED.clear();
        TRACKED.clear();
        clearCache();
    }

    public void clearCache() {
        LootBookWidget.getInstance().clearCache();
    }

    public void debug(Item item) {
        Set<Trackable<Identifier>> tracked = TRACKED.getOrDefault(Registries.ITEM.getId(item), Sets.newHashSet());
        RandoAssistant.LOGGER.info("Tracking " + Registries.ITEM.getId(item));
        for(Trackable<Identifier> trackable : tracked) {
            RandoAssistant.LOGGER.info(trackable.getIdentifier() + " -> " + trackable.getOutput() + " (enabled: " + trackable.isEnabled() + ")");
        }
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

        public final Set<Trackable<T>> getEnabled() {
            Set<Trackable<T>> enabled = Sets.newHashSet();
            for(Trackable<T> trackable : trackables.values()) {
                if(trackable.isEnabled()) {
                    enabled.add(trackable);
                }
            }
            return enabled;
        }

        public void clear() {
            trackables.clear();
        }

        public void enableAll() {
            trackables.values().forEach(Trackable::enableOverride);
        }

        public void disableAll() {
            trackables.values().forEach(Trackable::disableOverride);
        }

        @Override
        public String toString() {
            return "TrackableMap{" +
                    "trackables=" + trackables +
                    '}';
        }
    }
}
