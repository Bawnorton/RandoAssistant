package com.bawnorton.randoassistant.tracking.trackable;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.search.Searchable;
import com.bawnorton.randoassistant.util.IdentifierType;
import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Trackable<T> implements Comparable<Trackable<T>>, Searchable {
    private final Stat<T> associatedStat;
    private final Identifier identifier;
    private final Set<Identifier> output = Sets.newHashSet();

    private final IdentifierType identifierType;
    private final StatHandler statHandler;

    private boolean enabledOverride = false;

    public Trackable(Stat<T> associatedStat, Identifier identifier) {
        this.associatedStat = associatedStat;
        this.identifier = identifier;
        this.identifierType = IdentifierType.fromId(identifier);
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
        return statHandler.getStat(getStat()) > 0 || enabledOverride;
    }

    public Stat<T> getStat() {
        return associatedStat;
    }

    public Identifier getLootTableId() {
        T value = associatedStat.getValue();
        if(value instanceof Identifier id) {
            return id;
        }
        return new Identifier(RandoAssistant.MOD_ID, "invalid");
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
        return "Trackable{of=" + getLootTableId()
                + ", enabled=" + isEnabled()
                + "}";
    }

    public void enableOverride() {
        enabledOverride = true;
    }

    public void disableOverride() {
        enabledOverride = false;
    }

    @Override
    public Set<String> getSearchableStrings() {
        Set<String> strings = Sets.newHashSet();
        getOutput().forEach(id -> {
            strings.add(id.getPath());
            strings.add(switch (IdentifierType.fromId(id)) {
                case BLOCK -> Registries.BLOCK.get(identifier).getName().getString();
                case ITEM -> Registries.ITEM.get(identifier).getName().getString();
                case ENTITY -> Registries.ENTITY_TYPE.get(identifier).getName().getString();
                case OTHER -> id.getPath();
            });
        });
        return strings;
    }
}
