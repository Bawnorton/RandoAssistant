package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.search.Searchable;
import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class Trackable<T> implements Comparable<Trackable<T>>, Searchable {
    private final Stat<T> associatedStat;
    private final Set<Trackable<?>> sources = Sets.newHashSet();
    protected final StatHandler statHandler;

    private boolean enabledOverride = false;

    protected Trackable(Stat<T> associatedStat) {
        this.associatedStat = associatedStat;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        statHandler = player.getStatHandler();
    }

    public void addSource(Trackable<?> source) {
        sources.add(source);
    }

    public Set<Trackable<?>> getEnabledSources() {
        return sources.stream().filter(Trackable::isEnabled).collect(Collectors.toSet());
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
    public int compareTo(@NotNull Trackable<T> tTrackable) {
        return getIdentifier().compareTo(tTrackable.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Trackable<?> other) {
            return other.getIdentifier().equals(getIdentifier());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Trackable{of=" + getContent()
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
