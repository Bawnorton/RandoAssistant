package com.bawnorton.randoassistant.tracking.trackable;

import com.google.common.collect.Sets;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

import java.util.Set;

public class KilledTrackable extends Trackable<EntityType<?>> {
    public KilledTrackable(Stat<EntityType<?>> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ENTITY_TYPE.getId(getContent());
    }

    @Override
    public Set<String> getSearchableStrings() {
        Set<String> strings = Sets.newHashSet();
        strings.add(getIdentifier().toString());
        strings.add(getContent().getName().getString());
        return strings;
    }
}
