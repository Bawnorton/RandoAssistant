package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.config.Config;
import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Trackable<T> implements Comparable<Trackable<T>> {
    private final Stat<T> associatedStat;
    private final Identifier identifier;
    private final Set<Identifier> output = Sets.newHashSet();

    private final StatHandler statHandler;

    public Trackable(Stat<T> associatedStat, Identifier identifier) {
        this.associatedStat = associatedStat;
        this.identifier = identifier;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        statHandler = player.getStatHandler();
    }

    public Set<Identifier> getOutput() {
        return output;
    }

    public void addOutput(Identifier id) {
        output.add(id);
    }

    public boolean isEnabled() {
        return statHandler.getStat(getStat()) > 0 || Config.getInstance().enableOverride;
    }

    public Stat<T> getStat() {
        return associatedStat;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public int compareTo(@NotNull Trackable<T> other) {
        return getIdentifier().compareTo(other.getIdentifier());
    }

    @Override
    public int hashCode() {
        int idHash = getIdentifier().hashCode();
        int statHash = getStat().hashCode();
        return idHash ^ statHash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Trackable<?> other) {
            boolean sameId = other.getIdentifier().equals(getIdentifier());
            boolean sameStat = other.getStat().equals(getStat());
            return sameId && sameStat;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Trackable{of=" + getIdentifier()
                + ", stat=" + getStat()
                + ", output=" + getOutput()
                + ", enabled=" + isEnabled()
                + "}";
    }
}
