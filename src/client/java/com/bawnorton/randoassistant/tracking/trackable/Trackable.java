package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;

public abstract class Trackable<T> {
    private final Stat<T> associatedStat;
    protected final StatHandler statHandler;

    private boolean enabledOverride = false;

    protected Trackable(Stat<T> associatedStat) {
        this.associatedStat = associatedStat;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        statHandler = player.getStatHandler();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Trackable<V>, V> T of(Stat<V> stat) {
        if(stat.getValue() instanceof Block) {
            return (T) new MinedTrackable((Stat<Block>) stat);
        } else if(stat.getValue() instanceof Item) {
            return (T) new PickedUpTrackable((Stat<Item>) stat);
        } else if(stat.getValue() instanceof EntityType<?>) {
            return (T) new KilledTrackable((Stat<EntityType<?>>) stat);
        } else {
            throw new UnsupportedOperationException("Unknown stat type: " + stat.getValue());
        }
    }

    public boolean isEnabled() {
        return statHandler.getStat(getStat()) > 0 || enabledOverride;
    }

    public Stat<T> getStat() {
        return associatedStat;
    }

    public Object getWrapped() {
        return getStat().getValue();
    }

    @Override
    public String toString() {
        return "Trackable{stat=" + associatedStat
                + ", enabled=" + isEnabled()
                + "}";
    }

    public void enableOverride() {
        enabledOverride = true;
    }

    public void disableOverride() {
        enabledOverride = false;
    }
}
