package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

public class LootedTrackable extends Trackable<Identifier> {
    public LootedTrackable(Stat<Identifier> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return getContent();
    }
}
