package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

public class KilledTrackable extends Trackable<EntityType<?>> {
    public KilledTrackable(Stat<EntityType<?>> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ENTITY_TYPE.getId(getContent());
    }
}
