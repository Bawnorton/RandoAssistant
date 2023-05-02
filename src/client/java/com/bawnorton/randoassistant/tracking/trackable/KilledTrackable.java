package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.entity.EntityType;
import net.minecraft.stat.Stat;

public class KilledTrackable extends Trackable<EntityType<?>> {
    protected KilledTrackable(Stat<EntityType<?>> associatedStat) {
        super(associatedStat);
    }
}
