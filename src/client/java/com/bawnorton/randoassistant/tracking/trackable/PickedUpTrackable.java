package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

public class PickedUpTrackable extends Trackable<Item> {
    public PickedUpTrackable(Stat<Item> associatedStat) {
        super(associatedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.ITEM.getId(getContent());
    }
}