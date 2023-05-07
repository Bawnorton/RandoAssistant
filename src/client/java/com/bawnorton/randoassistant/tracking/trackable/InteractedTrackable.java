package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

public class InteractedTrackable extends Trackable<Block> {
    public InteractedTrackable(Stat<Block> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.BLOCK.getId(getContent());
    }
}
