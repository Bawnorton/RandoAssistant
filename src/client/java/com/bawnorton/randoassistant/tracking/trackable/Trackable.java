package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;

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

    public boolean isEnabled() {
        return statHandler.getStat(getStat()) > 0 || enabledOverride;
    }

    public Stat<T> getStat() {
        return associatedStat;
    }

    public T getContent() {
        return associatedStat.getValue();
    }

    public abstract Identifier getIdentifier();

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
